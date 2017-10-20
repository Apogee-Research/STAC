import os
import random
import shutil

import sys
import time
sys.path.append('../../../examples')

import interact

class FilterTest(object):
    def __init__(self,  hostname, port, username, password, image_rel_path):
        self.hostname = hostname
        self.port = port
        self.image_rel_path = image_rel_path
        self.snap_caller = interact.SnapCaller(hostname, port, username, password, ignore_images=True)

    def login(self, session):
        self.snap_caller.process_login(session)

    def test_filter(self, session, filter_ids):
        # apply the filter
        response = self.snap_caller.do_filter(session, self.image_rel_path, filter_ids)
        assert response.status_code == 200

        # get image url
        rel_path = "photo/" + self.image_rel_path
        start = time.time()
        response = self.snap_caller.do_image(session, rel_path)
        stop = time.time()
        return (stop - start, response)

    def test_filters(self, session, ids, savedir=None):
        # tests all the filter ids in ids
        for id in ids:
            print "Testing:", id,
            (duration, response) = self.test_filter(session, [id])
            assert response.status_code == 200
            print ": ", duration
            if savedir:
                self.save_photo(savedir, response, id)

    def save_photo(self, savedir, response, name):
        filename = os.path.join(savedir, "{}.jpg".format(name))
        with open(filename, 'wb') as out:
            for chunk in response:
                out.write(chunk)

    def test_many_filters(self, session, ids, num_filters, num_tests, savedir=None):
        for test in xrange(num_tests):
            filters = ['F00E', 'F00F']
            for filter in filters:
                if filter in ids:
                    ids.remove(filter)
            sampled_filters = random.sample(ids, num_filters)
            filters.extend(sampled_filters)
            print "Testing:", filters,
            (duration, response) = self.test_filter(session, filters)
            assert response.status_code == 200
            print ": ", duration
            if savedir:
                self.save_photo(savedir, response, "_".join(filters))

if __name__ == "__main__":
    if len(sys.argv) < 5:
        print "findcities.py <hostname> <port> <username> <password> <image path>"
        sys.exit(-1)
    caller = FilterTest(sys.argv[1], int(sys.argv[2]), sys.argv[3], sys.argv[4], sys.argv[5])
    ids = ["F%03X" % (num,) for num in xrange(21)]
    with interact.get_session() as session:
        caller.login(session)
        caller.test_filters(session, ids, '.')
        caller.test_many_filters(session, ids, 2, 6, '.')
