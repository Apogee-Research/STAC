from comms_client import CommsClient
from malicious_connection import MaliciousConnection

class MaliciousClient(CommsClient):

    def connect(self, host, port, handler):
        self.connection = MaliciousConnection(host, port, handler, self.name, self.rsa)
        return self.connection

