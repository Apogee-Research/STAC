#!/usr/bin/env python

"""Query the GraphHopper server."""

import argh
from argh import arg
import requests
requests.packages.urllib3.disable_warnings()


DEFAULT_HOST = '127.0.0.1'
DEFAULT_PORT = 8989


class GHServer(object):

    def __init__(self, host=DEFAULT_HOST, port=DEFAULT_PORT):
        self.base_url = 'https://%s:%d' % (host, port)

    def places(self):
        """Query the /places endpoint on the server."""
        r = requests.get(self.base_url + '/places', verify=False)
        r.raise_for_status()
        return r

    def tour(self, places):
        """Query the /tour endpoint with a list of place names."""
        params = {
            'point': places,
        }
        r = requests.get(self.base_url + '/tour', params=params, verify=False)
        r.raise_for_status()
        return r


if __name__ == '__main__':
    import argh

    def places(host=DEFAULT_HOST, port=DEFAULT_PORT):
        r = GHServer(host, port).places()
        return r.json()

    @argh.arg('places', nargs='+', metavar='place')
    def tour(places, host=DEFAULT_HOST, port=DEFAULT_PORT):
        r = GHServer(host, port).tour(places)
        return r.text

    argh.dispatch_commands([places, tour])
