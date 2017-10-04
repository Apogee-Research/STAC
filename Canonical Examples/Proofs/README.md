These proofs of vulnerabilities are setup for the reference network environment described
in the reference network environment document. The python code is written in python3.5
with depencies: Matplotlib and Numpy.

These proofs use the following aliases
======================================
Alias		IP Address
-------------------------------
NUC1Local	192.168.100.10
NUC2Local	192.168.100.20
NUC3Local	192.168.100.30


SC Proofs Run Order
===================
1. Start server script
2. sudo python3.5 SC_Data_Client.py
3. python3.5 SC_Attacker_Client.py

ACT Proofs Run Order
====================
1. Start server script
2. sudo python3.5 ACT_Data_Client.py
3. python3.5 ACT_Benign_Client.py
4. python3.5 ACT_Attacker_Client.py


