All python code is written in python3.5 with dependencies: Matpotlib, Numpy

SC Time Analysis
================
    Figures/ - contains figures used in Tour Planner White Paper
    Results/ - contains data collected and used in white paper
               should be able to regenerate all data and results using the code in this directory
               note: some analysis code may take some time and use several GB of disk space
 
    Collect Edge Timing Data
    ========================
    1. Timing_Encrypted.py - collect edge times. Generates "Results/Edge Times Data Encrypted.p"
       used by all further analysis.

    Process Edge Timing Data
    ========================
    1. Initial Analysis Packet Timing.py - performs initial analysis of edge timings and
       generates "Results/Mean_Stdev_Data_Packet_Time_Encrypted.p" file used to guess the
       user's tour request.

    2. Initial Analysis Packet Timing_Mixed_Gaussian.py - experimentation with mixed 
       Gaussian model of data. Decided to abandon as existing model was sufficient.
       Not necessary for analysis but if choose to run, needs Sklearn module

    Guess The User Request
    ======================
    Code to implement MST guessing strategies discussed in Tour Planner White Paper

    1. Guess Tour All Methods Sample.py - performs random sampling of possible tours and 
       uses stored data determine the request

    2. Guess Tour All Methods.py - collects times and determines request using identified 
       best and worst cases

    3. Parallelized guessing of user request:
           Collect Data
           ============
           Guess Tour All Methods_Collect_Data.py - runs in serial, collecting timing data on 
           user tours. Generates "Results/Guess Tour All Methods DataSet Encrypted.p" used by
           the parallel guessing scripts described in "Process Data" below.

           Process Data
           ============
           Note: all runs in parallel with option to specify number of processes.
           Processes data in parallel using:
               1. MST guessing strategies - Guess Tour All Methods_Process_Data.py
               2. Edge-by-edge guessing strategies - Guess Tour Process_Data_Edge_by_Edge.py


    Worst Case Secret
    =================
    Code to determine the worst-case value of the secret. Note: all runs in parallel with
    option to specify number of processes.
    
    1. Worst Case Analysis Side Channel Bhattacharya Distance.py - identifies the worst-case
       secret using Bhattacharya distance metric as discussed in Tour Planner White Paper
    2. Worst Case Analysis Side Channel Mahalanobis Distance.py - identifies the worst-case
       secret using Mahalanobis distance metric as discussed in Tour Planner White Paper
    3. Worst Case Analysis Side Channel Overlap.py - identifies the worst-case
       secret using the overlap analysis as discussed in Tour Planner White Paper


SC Space Analysis
=================
1. Packet_Size_Query.py - collect packet size information on possible tours
2. Process Packet Size Query.py - process collected packet size data
