import re
import sys
sys.path.append('../examples')

import interact_gab

class Search(object):
    '''
    This class is just an extra layer between searchattack.py and interact_gab.py.  
    '''

    def __init__(self, hostname, port, username, password):
        '''
        Creates an instance.

        :param hostname: name of the server
        :param port: int port number of the server
        :param username: name of client user
        :param password: password of client user
        '''
        self.hostname = hostname
        self.port = port
        self.gab_caller = interact_gab.GabCaller(hostname, port, username, password)
            
    def do_search(self, search_term):
        '''
        Connects and interacts with the GabFeed client.
        The sequence of calls is as follows:
        1. Call "/" and get redirected to the /authenticate page
        2. POST to /authenticate and get redirected to the /login page
        3. POST to /login and get redirected to the rooms page
        4. POST to search
        5. End of session
    
        Assertions are included for each call to verify that the
        expected flow is followed.

        '''
        with interact_gab.get_session() as session:
            response = self.gab_caller.do_authenticate(session)
            response = self.gab_caller.do_login(session, response.url)
            response = self.gab_caller.do_search(session, search_term)


if __name__ == "__main__":
    if len(sys.argv) < 5:
        print "search.py <hostname> <port> <username> <password>"
        sys.exit(-1)
    caller = Search(sys.argv[1], int(sys.argv[2]), sys.argv[3], sys.argv[4])
    caller.find_all_search_terms()
