#
# Copyright (c) 2011 rPath, Inc.
#

import time
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
    ERR_TIMEOUT = 0x100
    ERR_FILE_NOT_FOUND = 0x200
    ERR_ACCESS_DENIED = 0x500
    ERR_ACCESS_DENIED2 = 0xBD00
    ERR_BAD_CREDENTIALS = 0x6D00

    __slots__ = ()

    _msgs = {
        ERR_TIMEOUT:
            'Timeout waiting for a response.',
        ERR_FILE_NOT_FOUND:
            'The file or registry key/value pair cannot be found.',
        ERR_ACCESS_DENIED:
            'The credentials provided do not have permission '
            'to access the requested resource. If this system is runnin '
            'Windows 2008 R2, please refer to the \'rPath Platform Guilde for '
            'Microsoft Windows\' for special configuration requirements '
            'necessary to enable remote WMI access.',
        ERR_ACCESS_DENIED2:
            'The credentials provided do not have permission to access the '
            'requested resource.',
        ERR_BAD_CREDENTIALS:
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
        ERR_FILE_NOT_FOUND: WMIFileNotFoundError,
        ERR_ACCESS_DENIED: WMIAccessDeniedError,
        ERR_ACCESS_DENIED2: WMIAccessDeniedError,
        ERR_BAD_CREDENTIALS: WMIBadCredentialsError,
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


class WMIClient(object):
    """
    Python frontend to the wmiclient command line.
    """

    _ErrorClass = WMIErrorCodes

    class _Results(namedtuple('WMICResults', 'host rc stdout stderr')):
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
            return self._split(self.stdout)

        @property
        def error(self):
            return self._split(self.stderr)


    def __init__(self, authInfo, callback=None):
        self._authInfo = authInfo

        if callback is None:
            callback = WMICallback(self._authInfo)
        self._cb = callback

        self._errors = self._ErrorClass()

        self._wmicCmd = [
            '/usr/bin/wmic',
            '--host', '%(host)s',
            '--user', '%(user)s',
            '--password', '%(password)s',
            '--domain', '%(domain)s',
        ]

    def __call__(self, *args):
        """
        Call the WMI Client with the specified arguments and the default set of
        authentication related informaiton.
        @return WMICResults(rc, stdout, stderr)
        """

        info = self._authInfo._asdict()
        cmd = [ x % info for x in itertools.chain(self._wmicCmd, args) ]

        p = subprocess.Popen(cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE)

        rc = None
        while True:
            rc = p.poll()
            if rc is not None:
                break

            self._cb.setStatus('Waiting for reponse from remote Windows server')
            time.sleep(1)

        if rc is not None and rc == 0:
            self._cb.setStatus('WMI call completed successfully')
        else:
            self._cb.setStatus('Error in WMI call')

        return self._Results(self._authInfo.host, rc,
            p.stdout.read(), p.stderr.read())

    def _request(self, *args):
        result = self(*args)
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
            [ y.strip() for y in x.split(',') ] for x in result.iteroutput()
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
        result = self('registry', 'getkey', keyPath, key)

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
