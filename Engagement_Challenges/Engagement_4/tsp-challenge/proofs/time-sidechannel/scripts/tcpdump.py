import sys
import subprocess
import time

from ghserver import DEFAULT_PORT
from util import getLogger

_logger = getLogger(__name__)


class TcpDump(object):

    def __init__(self, output_file, interface=None, port=DEFAULT_PORT):
        _logger.info("capturing packets to %s", output_file)

        if not interface:
            if sys.platform.startswith('darwin'):
                interface = 'lo0'
            else:
                interface = 'lo'

        cmd = ['sudo', 'tcpdump', '-n']
        if interface:
            cmd += ['-i', interface]
        cmd += ['-w', output_file]
        cmd += ['tcp src port %d' % port]

        # pre-authenticate
        subprocess.check_call(['sudo', '-v'])

        self._proc = subprocess.Popen(cmd)

        time.sleep(1)

    def close(self):
        if self._proc:
            _logger.info("stopping tcpdump")
            time.sleep(1)
            subprocess.check_call(['sudo', 'kill', str(self._proc.pid)])
            self._proc.wait()
            self._proc = None

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
