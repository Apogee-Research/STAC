# MIT License
#
# Copyright (c) 2017 Apogee Research
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

import time
import socket
import struct
import sys
import pickle
import numpy as np


def send(request_list, expected_packets, timeout, term_packet):
    # Request List list of requests -> [Text Data, Bytes]
    # timeout -> seconds
    # expected_packets and term_packet control termination
    #   expected_packets -> Num Expected | term_packet -> "NA"
    #   expected_packets -> inf          | term_packet -> [expected term packet(s)]

    host = '127.0.0.1'
    port = 7688
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        response_list = []
        for request in request_list:
            print(request[0])
            sys.stdout.flush()
            start = time.time()
            s.sendto(request[1], (host, port))
            received_packets = 0
            while received_packets < expected_packets and time.time() - start < timeout:
                bytes_in = s.recvfrom(4)
                stop = time.time()
                response = int.from_bytes(bytes_in[0], byteorder='big', signed=True)
                response_list.append([request[0], response, stop - start])
                received_packets += 1
                if term_packet != "NA" and response in term_packet:
                    break
            time.sleep(0.02)
        return response_list
    except:
        print(sys.exc_info()[0])
        sys.stdout.flush()
        raise RuntimeError("Socket Timeout")


def login():
    request = struct.pack('>cixcxcxcxcxcxc', bytes([23]), 6, 'p'.encode('ascii'),
                          'i'.encode('ascii'), 'c'.encode('ascii'),
                          'a'.encode('ascii'), 'r'.encode('ascii'), 'd'.encode('ascii'))
    response = send([['Login: Picard', request]], 2, 2, "NA")
    if response[0][1] != 1:
        raise RuntimeError("Login Error")


def init(session_ID):
    if type(session_ID) != int:
        raise RuntimeError("Invalid Input")
    request = struct.pack('>ci', bytes([13]), session_ID)
    response = send([['Init Sandbox Session: ' + str(session_ID), request]], 1, 2, "NA")
    if response[0][1] != 1:
        raise RuntimeError("Init Error")


def commit(session_ID):
    if type(session_ID) != int:
        raise RuntimeError("Invalid Input")
    request = struct.pack('>ci', bytes([14]), session_ID)
    response = send([['Commit Sandbox Session: ' + str(session_ID), request]], 1, 2, "NA")
    if response[0][1] != 1:
        raise RuntimeError("Commit Error")


def insert(session_ID, keys):
    if any(type(i) != int for i in [session_ID] + keys):
        raise RuntimeError("Invalid Input")

    def pack_key(session_ID, my_key):
        return struct.pack('>cii', bytes([3]), session_ID, my_key)

    requests = []
    for key in keys:
        request = pack_key(session_ID, key)
        requests.append(['Insert: ' + str(key), request])
    responses = send(requests, 2, 5, "NA")
    return responses


def search(session_ID, MIN, MAX):
    if any(type(i) != int for i in [session_ID, MIN, MAX]):
        raise RuntimeError("Invalid Input")
    request = struct.pack('>ciii', bytes([11]), session_ID, MIN, MAX)
    response = send([['Search: ' + str(MIN) + ", " + str(MAX), request]], float('inf'), 2, [-8, -1])
    return response


def test():
    test_inserts = [200005, 200007]
    session_ID = login()
    init(session_ID)
    search_1 = np.array(search(session_ID, 0, 40000000))
    insert_data = insert(session_ID, test_inserts)
    search_2 = np.array(search(session_ID, 0, 40000000))
    commit(session_ID)

    stat1 = len(insert_data) == (len(search_2) - len(search_1))
    stat2 = all(str(i) in search_2[:, 1] for i in test_inserts)
    return stat1 and stat2


def save_data(data, filename):
    pickle.dump(data, open(filename, "wb"))


def main():
    v_nv = "NV"
    if len(sys.argv) == 2:
        trial = sys.argv[1]
    else:
        trial = ""

    insert_list = list(range(200005, 202405))

    session_ID = login()
    init(session_ID)
    search_1 = np.array(search(session_ID, 0, 40000000))
    insert_data = insert(session_ID, insert_list)
    search2 = np.array(search(session_ID, 0, 40000000))
    commit(session_ID)
    print("Items Inserted:", len(insert_list), "Packets:", len(insert_data))
    print("Verified Inserts:", (len(search2) - len(search_1)))

    # Process Insert Data
    time_data = []
    completed = []
    for data in insert_data:
        if data[0] not in completed:
            time_data.append(data[2])
            completed.append(data[0])

    save_data([search_1, insert_data, search2], "Input Data/Python/" + v_nv + "rawData" + trial + ".p")
    save_data(time_data, "timeData" + trial + ".p")


if __name__ == "__main__":
    main()
