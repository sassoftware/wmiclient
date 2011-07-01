#
# Copyright (c) 2011 rPath, Inc.
#

import sys
import time
import errno
import socket
import itertools
import subprocess
from collections import namedtuple

class WindowsAuthInfo(namedtuple('WindowsAuthInfo', 'host domain user password')):
    __slots__ = ()

    def __new__(cls, host, domain, user, password):
        host = cls._getIp(host)
        return tuple.__new__(cls, (host, domain, user, password))

    @staticmethod
    def _getIp(host):
        """
        Get the IP address of a given host.
        """

        name, _, addressList = socket.gethostbyaddr(host)
        assert len(addressList)
        return addressList[0]


class NetworkInterface(namedtuple('NetworkInterface', 'name ip_address '
    'netmask dns_name required')):

    __slots__ = ()

    @property
    def cidr(self):
        if '.' not in self.netmask:
            return int(self.netmask)

        octets = [int(x) for x in self.netmask.split('.') ]
        cidr = 0
        for i in octets:
            while i:
                cidr += i & 1
                i >>= 1
        return cidr

    @property
    def isv6(self):
        return ':' in self.ip_address

    @property
    def isv4(self):
        return not self.isv6


class WMICallback(object):
    __slots__ = ('_host', )

    def __init__(self, authInfo):
        self._host = authInfo.host

    def _prependHost(self, msg):
        return '%s: %s' % (self._host, msg)

    def setStatus(self, msg):
        pass

    def debug(self, msg):
        pass


class WMIBaseError(Exception):
    def __init__(self, result, *args, **kwargs):
        self.result = result
        Exception.__init__(self, *args, **kwargs)


class WMITimeoutError(WMIBaseError):
    pass


class WMIFileNotFoundError(WMIBaseError):
    pass


class WMIAccessDeniedError(WMIBaseError):
    pass


class WMIBadCredentialsError(WMIBaseError):
    pass


class WMIUnknownError(WMIBaseError):
    pass


class WMIErrorCodes(object):
    UNKNOWN = -1
    ERR_TIMEOUT = 0x000005B4
    WAIT_TIMEOUT = 0x00000102
    ERROR_FILE_NOT_FOUND = 0x00000002
    STATUS_LOGON_FAILURE = 0xC000006D
    ERROR_WRONG_PASSWORD = 0x0000052B

    __slots__ = ()

    _msgs = {
        ERR_TIMEOUT:
            'Timeout waiting for a response.',
        WAIT_TIMEOUT:
            'Timeout waiting for a response.',
        ERROR_FILE_NOT_FOUND:
            'The file or registry key/value pair cannot be found.',
        STATUS_LOGON_FAILURE:
            'The credentials provided do not have permission '
            'to access the requested resource. If this system is runnin '
            'Windows 2008 R2, please refer to the \'rPath Platform Guilde for '
            'Microsoft Windows\' for special configuration requirements '
            'necessary to enable remote WMI access.',
        ERROR_WRONG_PASSWORD:
            'The username, password, or domain is invalid.',
        UNKNOWN:
            'Undefined error code.',
    }
    _default_message = _msgs[UNKNOWN]

    _msg_template = """\
WMIClient Error, client returned code %(rc)s: %(msg)s
"""

    _exceptions = {
        ERR_TIMEOUT: WMITimeoutError,
        WAIT_TIMEOUT: WMITimeoutError,
        ERROR_FILE_NOT_FOUND: WMIFileNotFoundError,
        STATUS_LOGON_FAILURE: WMIAccessDeniedError,
        ERROR_WRONG_PASSWORD: WMIBadCredentialsError,
        UNKNOWN: WMIUnknownError,
    }
    _default_exception = _exceptions[UNKNOWN]

    @classmethod
    def __getitem__(cls, result):
        return cls.error(result)
    get = __getitem__

    @classmethod
    def __contains__(cls, result):
        return result.rc in cls._msgs

    @classmethod
    def error(cls, result):
        errorCls = cls._exceptions.get(result.rc, cls._default_exception)
        msg = cls._msgs.get(result.rc, cls._default_message)
        message = cls._msg_template % {'rc': result.rc, 'msg': msg}
        return errorCls(result, message)


class WMICResults(namedtuple('WMICResults', 'host rc stdout stderr')):
    """
    Class for storing WMI Client results.
    """

    __slots__ = ()

    @staticmethod
    def _split(input):
        output = []
        for line in input.split('\n'):
            line = line.strip()
            if line:
                output.append(line)
        return output

    @property
    def output(self):
        if isinstance(self.stdout, list):
            return self.stdout
        return self._split(self.stdout)

    @property
    def error(self):
        return self._split(self.stderr)


class AbstractCommand(object):
    _wmicCmd = (
        '/usr/bin/wmic',
#        'java', '-classpath',
#        'wmiclient.jar:commons-cli-1.3-SNAPSHOT.jar:jcifs-1.2.19.jar:j-interopdeps.jar:j-interop.jar',
#        'com.rpath.management.windows.WMIClientCmd', 
        '--host', '%(host)s',
        '--user', '%(user)s',
        '--password', '%(password)s',
        '--domain', '%(domain)s'
    )

    def __init__(self, authInfo, callback):
        self._authInfo = authInfo
        self._callback = callback

    def execute(self, *args):

        """
        Call the WMI Client with the specified arguments and the default set of
        authentication related informaiton.
        @return WMICResults(rc, stdout, stderr)
        """

        self._run(args)
        rc, output, error = self._parseOutput()

        return WMICResults(self._authInfo.host, rc, output, error)

    def _run(self, args):
        raise NotImplementedError

    def _parseOutput(self):
        raise NotImplementedError


class DefaultCommand(AbstractCommand):
    """
    Class for running wmic.
    """

    def _run(self, *args):
        info = self._authInfo._asdict()
        cmd = [ x % info for x in itertools.chain(self._wmicCmd, args) ]

        self._callback.debug('calling: %s' % (cmd, ))

        self._p = subprocess.Popen(cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE)

        self._rc = None
        while True:
            self._rc = self._p.poll()
            if self._rc is not None:
                break

            self._callback.setStatus('Waiting for reponse from remote Windows server')
            time.sleep(1)

        if self._rc is not None and self._rc == 0:
            self._callback.setStatus('WMI call completed successfully')
        else:
            self._callback.setStatus('Error in WMI call')

    def _parseOutput(self):
        return self._rc, self._p.stdout.read(), self._p.stderr.read()


class InteractiveCommand(AbstractCommand):
    """
    Class for communicating with the interactive version of wmic.
    """

    _MARKER = '= '
    _ERROR = '= ERROR'
    _START_OUTPUT = '= START OUTPUT'
    _END_OUTPUT = '= END OUTPUT'
    _START_STACKTRACE = '= START STACKTRACE'
    _END_STACKTRACE = '= END STACKTRACE'

    def __init__(self, *args, **kwargs):
        AbstractCommand.__init__(self, *args, **kwargs)

        self._wmicCmd = self._wmicCmd + ('--interactive', )

        self._p = None

    def _createProcess(self):
        info = self._authInfo._asdict()
        cmd = [ x % info for x in self._wmicCmd ]

        self._callback.debug('calling: %s' % (cmd, ))

        self._p = subprocess.Popen(cmd,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=sys.stderr,
#            cwd=os.environ['HOME'] + '/hg/wmiclient/dist/lib'
        )

    def _write(self, data):
        try:
            self._p.stdin.write(data)
        except IOError, e:
            # Restart the java vm if it dies.
            if e.errno == errno.EPIPE:
                self._createProcess()
                self._p.stdin.write(data)
            else:
                raise

    def _readline(self):
        return self._p.stdout.readline().strip()

    def _run(self, args):
        if not self._p:
            self._createProcess()

        cmd = ' '.join([ '"%s"' % x for x in args ])
        self._callback.debug(cmd)

        self._write(cmd)
        self._write('\n')

    def _parseOutput(self):
        line = self._readline()
        assert line.startswith(self._MARKER)

        rc = -1
        output = []
        error = []
        if line.startswith(self._ERROR):
            error.append(line[len(self._ERROR)+1:])
            if error[-1][0].isdigit():
                rc = int(error[-1][0])
            elif error[-1][0].startswith('0x'):
                rc = int(error[-1][0], 16)
        elif line.startswith(self._START_OUTPUT):
            rc = int(line[len(self._START_OUTPUT):])

            line = self._readline()
            while not line.startswith(self._MARKER):
                output.append(line)
                line = self._readline()
            assert line == self._END_OUTPUT
        elif line.startswith(self._START_STACKTRACE):
            line = self._readline()
            while not line.startswith(self._MARKER):
                error.append(line)
                line = self._readline()
            assert line == self._END_STACKTRACE

        if len(output) == 1 and output[0] == '':
            output = []

        if len(error) == 1 and error[0] == '':
            error = []

        return rc, output, error



class WMIClient(object):
    """
    Python frontend to the wmiclient command line.
    """

    _ErrorClass = WMIErrorCodes

    def __init__(self, authInfo, callback=None, interactive=True):
        self._authInfo = authInfo

        if callback is None:
            callback = WMICallback(self._authInfo)
        self._callback = callback

        if interactive:
            self._cmd = InteractiveCommand(self._authInfo, self._callback)
        else:
            self._cmd = DefaultCommand(self._authInfo, self._callback)

        self._errors = self._ErrorClass()

    def _request(self, *args):
        result = self._cmd.execute(*args)
        if result.rc:
            raise self._errors.get(result)
        return result

    def _service(self, action, service):
        return self._request('service', action, service)

    def _query(self, action):
        return self._request('query', action)

    def serviceStart(self, service):
        return self._service('start', service)

    def serviceStop(self, service):
        return self._service('stop', service)

    def serviceQuery(self, service):
        return self._service('getstatus', service)

    def queryNetwork(self):
        result = self._query('network')

        net_info = [
            [ y.strip() for y in x.split(',') ] for x in result.output
        ]

        interfaces = []
        for name, ipaddr, netmask, hostname, domain in net_info:
            hostname = hostname.lower()

            if domain:
                dns_name = '%s.%s' % (hostname, domain)
            else:
                dns_name = hostname

            host = self._authInfo.host
            required = ipaddr == host or dns_name == host

            interfaces.append(NetworkInterface(name, ipaddr, netmask,
                dns_name, required))

        return result, interfaces

    def queryUUID(self):
        result = self._query('uuid')

        output = result.output
        count = len(output)
        if count == 1:
            return result, output[0]
        else:
            raise WMIUnknownError, 'Found incorrect number of uuids'

    def registryGetKey(self, keyPath, key, ignoreExceptions=False):
        result = self._cmd.execute('registry', 'getkey', keyPath, key)

        if result.rc and not ignoreExceptions:
            raise self._errors.get(result)
        return result

    def registrySetKey(self, keyPath, key, value):
        if not isinstance(value, list):
            value = [value, ]
        if not value:
            value = ['', ]

        return self._request('registry', 'setkey', keyPath, key, *value)

    def registryCreateKey(self, keyPath, key):
        return self._request('registry', 'createkey', keyPath, key)

    def processCreate(self, cmd):
        return self._request('process', 'create', cmd)

    def processStatus(self, pid):
        return self._request('process', 'status', pid)
