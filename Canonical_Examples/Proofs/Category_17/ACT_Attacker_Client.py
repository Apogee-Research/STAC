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


import json
import multiprocessing as mp
import os
import pickle
import socket
import sys
import time
import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import math
import numpy as np


def establish_benign():
    benign_host = 'clientNuc'
    benign_port = 9091
    try:
        s_ = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s_.connect((benign_host, benign_port))
        return s_
    except:
        print("Error: Benign Establish")
        sys.exit(-1)


def establish_data():
    data_host = 'masterNuc'
    data_port = 9090
    try:
        s_ = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s_.connect((data_host, data_port))
        return s_
    except:
        print("Error: Data Establish")
        sys.exit(-1)


def send_cmd(c_, message):
    to_send = message + "\r"
    c_.sendall(to_send.encode())


def receive_cmd(c_):
    msg_char = ""
    message = ""
    while msg_char != "\r":
        msg_char = c_.recv(1).decode()
        message += msg_char
    return message.strip()


def close_cmd(c_):
    c_.close()


def send_request(remote_host, remote_port, result_queue, request, sleep_time, timeout):
    time.sleep(sleep_time)
    result = ""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((remote_host, remote_port))
        to_send = request + '\n'
        a = time.time()
        s.sendall(to_send.encode())
        s.settimeout(timeout)
        response_char = ""
        response = ""
        while response_char != "\n":
            response_char = s.recv(1).decode()
            response += response_char
        response = response.strip()
        b = time.time()
        s.close()
        result = {"request": request, "response": response, "runtime": b - a}
    except:
        result = "Sample Error"
    finally:
        result_queue.put(result)


def attack_requests(remote_host, remote_port, mal_requests, s_benign_,
                    benign_request, timeout, sample):
    result_queue = mp.Queue()
    print("\t\tStarting Attacker Send")
    process_list = [mp.Process(target=send_request,
                               args=(remote_host, remote_port, result_queue,
                                     mal_request["request"],
                                     mal_request["sleep_time"], timeout),
                               daemon=True) for mal_request in mal_requests]
    for p in process_list:
        p.start()

    # Launch benign request
    time.sleep(0.1)
    send_benign_command_1 = {"BENIGN_Command": "Send",
                             "REMOTE_HOST": remote_host,
                             "REMOTE_PORT": remote_port,
                             "BENIGN_Request": benign_request,
                             "Timeout": timeout,
                             "Sample": sample}
    send_cmd(s_benign_, json.dumps(send_benign_command_1))
    print("\t\tStarting Benign Send")

    # Wait benign request
    benign_msg_in_1 = receive_cmd(s_benign_)
    benign_data_in_1 = json.loads(benign_msg_in_1)
    assert (benign_data_in_1["BENIGN_Status"] == "Response In")
    print("\t\tBenign Response Received")

    results = {"attack_response": [result_queue.get() for mal_request in mal_requests],
               "benign_response": benign_data_in_1}

    for p in process_list:
        p.join()
    for p in process_list:
        p.terminate()
    return results


def run_experiment(remote_host, remote_port, samples):
    timeout = None
    # Establish data
    s_data_ = establish_data()
    # Establish benign
    s_benign_ = establish_benign()

    # {attack_response, benign_response, data_in}    
    runtime_data_list = []

    # Start sampling
    max_items = 10000
    for sample in range(1, samples + 1, 1):
        print("Sample:", sample, "of", samples)
        sys.stdout.flush()

        # Send attack requests
        # Demonstrate both random max length and malicious ordered
        data_min_dict = {}
        for m_key in ["Avg", "Worst"]:
            # Inform data host to start
            send_data_command_1 = {"DATA_Command": "Start", "REMOTE_PORT": remote_port, "Sample": sample}
            send_cmd(s_data_, json.dumps(send_data_command_1))
            data_msg_in_1 = receive_cmd(s_data_)
            data_in_1 = json.loads(data_msg_in_1)
            assert (data_in_1["DATA_Status"] == "Started")
            print("\tA", "Starting")
            print("\t\tStarting Data Collection")
            sys.stdout.flush()

            if m_key == "Worst":
                mal_input = "".join([str(i) + "," for i in range(max_items)])
            else:
                mal_input = "".join([str(np.random.randint(999999)) + "," for i in range(max_items)])
            mal_requests = [{"request": mal_input, "sleep_time": 0}]
            benign_request = "2,1"
            runtime_data = attack_requests(remote_host, remote_port, mal_requests,
                                           s_benign_, benign_request, timeout, sample)
            benign_response = runtime_data["benign_response"]

            # Stop data collection
            send_data_command_2 = {"DATA_Command": "Stop", "Sample": sample}
            send_cmd(s_data_, json.dumps(send_data_command_2))
            data_msg_in_2 = receive_cmd(s_data_)

            # Process data host response
            data_in_2 = json.loads(data_msg_in_2)
            assert (data_in_2["DATA_Status"] == "Stopped")

            # Assess data from data host
            if benign_response["BENIGN_Response"] == "NA" or data_in_2["Sample_Status"] == "Sample Error":
                runtime_data["data_in"] = timeout
            else:
                runtime_data["data_in"] = data_in_2["Data"]["B_Time"]
            data_min_dict[m_key] = runtime_data["data_in"]
        runtime_data_list.append(data_min_dict)
        print("\tB", "Stopping")
        print("\t\tProceeding to Next")
        sys.stdout.flush()

    # Inform data server complete
    send_data_command_3 = {"DATA_Command": "Complete"}
    send_cmd(s_data_, json.dumps(send_data_command_3))
    data_msg_in_3 = receive_cmd(s_data_)
    data_in_3 = json.loads(data_msg_in_3)
    assert (data_in_3["DATA_Status"] == "Complete")
    close_cmd(s_data_)

    # Inform benign server complete
    send_benign_command_2 = {"BENIGN_Command": "Complete"}
    send_cmd(s_benign_, json.dumps(send_benign_command_2))
    benign_msg_in_2 = receive_cmd(s_benign_)
    benign_data_in_2 = json.loads(benign_msg_in_2)
    assert (benign_data_in_2["BENIGN_Status"] == "Complete")
    close_cmd(s_benign_)

    print("Complete")
    sys.stdout.flush()
    return runtime_data_list


def calc_percentile(data, percentile):
    my_data = sorted(data, reverse=True)
    index = int(math.floor(percentile * len(my_data) / 100))
    val = my_data[index]
    return val


def get_bins(my_data, a, b, n):
    """Get plot data"""
    bins = np.arange(a, b + (b - a) / n, (b - a) / n)
    h = plt.hist(my_data, bins=bins)
    plt.close()
    x = h[1][:-1]
    y = h[0]
    return [x, y]


def find_nearest_index(my_data, value):
    """Get nearest index value"""
    i = 0
    for j in range(len(my_data)):
        if abs(my_data[j] - value) < abs(my_data[j] - my_data[i]):
            i = j
    return i


def plot_data(data, fig_name, plot_type="Histogram"):
    assert (plot_type in ["Histogram", "Cumulative"])

    mal_x = []
    mal_y = []
    ben_x = []
    ben_y = []
    mal_data = [dat["Worst"] for dat in data]
    ben_data = [dat["Avg"] for dat in data]
    n = round(len(mal_data) / 25)
    size = 1

    if plot_type == "Histogram":
        # Benign Plot
        ben_hist = get_bins(ben_data, min(ben_data), max(ben_data[1:]), n)
        fig = plt.figure()
        plt.plot(ben_hist[0], ben_hist[1] / len(ben_data), c='b', lw=size)
        plt.plot([np.median(ben_data), np.median(ben_data)], [0, 2200], c='purple', ls='--', lw=1)
        plt.xlim([0, 2])
        plt.ylim([0, .25])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentage of Benign Runs")
        fig.savefig(fig_name + " Benign.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        # Malicious Plot
        mal_hist = get_bins(mal_data, min(mal_data), max(mal_data), n)
        fig = plt.figure()
        plt.plot(mal_hist[0], mal_hist[1] / len(mal_data), c='r', lw=size)

        # Shade Vulnerable Area
        n_i = find_nearest_index(mal_hist[0], 750)
        plt.fill_between(mal_hist[0][n_i:], [0 for i in mal_hist[1][n_i:]], mal_hist[1][n_i:] / len(mal_data),
                         color='grey', alpha='0.5')

        plt.plot([np.median(mal_data), np.median(mal_data)], [0, 140], c='purple', ls='--', lw=1)
        plt.plot([750, 750], [0, 140], c='black', ls='-', lw=1)
        plt.xlim([0, 2000])
        plt.ylim([0, .014])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentage of Malicious Runs")
        fig.savefig(fig_name + " Malicious.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        print("Result: Median\n\tAverage Input: ", np.median(ben_data))
        print("\tMalicious Input: ", np.median(mal_data))

    elif plot_type == "Cumulative":
        mal_50 = 0
        ben_50 = 0
        for percentile in np.arange(1, 100, 0.5):
            mal_runtime = calc_percentile(mal_data, percentile)
            mal_x.append(mal_runtime)
            mal_y.append(percentile)
            ben_runtime = calc_percentile(ben_data, percentile)
            ben_x.append(ben_runtime)
            ben_y.append(percentile)
            if percentile == 50:
                mal_50 = mal_runtime
                ben_50 = ben_runtime

        # Benign Plot
        fig = plt.figure()
        plt.plot(ben_x, ben_y, c='b', lw=size)
        plt.plot([ben_50, ben_50], [0, 102], c='b', ls='--', lw=1)
        plt.xlim([0, 1.1])
        plt.ylim([0, 100])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentile of Benign Runs")
        fig.savefig(fig_name + " Benign.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        # Malicious Plot
        fig = plt.figure()
        plt.plot(mal_x, mal_y, c='r', lw=size)
        plt.plot([mal_50, mal_50], [0, 102], c='r', ls='--', lw=1)
        plt.plot([900, 900], [0, 102], c='k', ls='--', lw=1)
        plt.plot([0, 2000], [50, 50], c='orange', ls='--', lw=1)
        plt.xlim([0, 2000])
        plt.ylim([0, 100])
        plt.grid()
        plt.xlabel("Resource Usage [Runtime (ms)]")
        plt.ylabel("Percentile of Malicious Runs")
        fig.savefig(fig_name + " Malicious.png", bbox_inches='tight', format='png', dpi=1200)
        plt.close()

        print("Result: 50th Percentile\n\tAverage Input: ", ben_50)
        print("\tMalicious Input: ", mal_50)


def save_results(filename, results_data):
    os.chdir("Data")
    with open(filename + ".p", "wb") as out_file:
        pickle.dump(results_data, out_file)
    os.chdir("..")


def main():
    remote_host = 'serverNuc'
    remote_port = 8000
    data_filename = "Data"

    print("Starting Sampling Host:", remote_host, ":", remote_port)
    sys.stdout.flush()
    results = run_experiment(remote_host, remote_port, 1000)
    plot_data(results, "Figures/Resource Usage")
    sys.stdout.flush()
    save_results(data_filename, results)


if __name__ == "__main__":
    main()
