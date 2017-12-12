Overview
========
The DARPA Space/Time Analysis for Cybersecurity (STAC) program aims to develop
semi-automated tools to identify algorithmic complexity and side channels in 
space (e.g. packet size) and time (e.g. server response time to request). The
programs in this repository were used to test the capability of different
research approaches to achieving the program goals. The Canonical examples are
simple programs that highlight different ways in which tools may miss potential
vulnerabilities. The Engagement Challenges are larger applications used to test
the tools' abilities to identify STAC vulnerabilities. The Common Reference
Platform documents contain the details on the platform for which these examples
and challenges were developed.

These programs are being released publicly to allow others to identify STAC
vulnerabilities in the programs and to develop their own tools to identify STAC
vulnerabilities.

Operational Definitions for STAC
================================
The STAC program uses the following definitions for Side Channel and Algorithmic
Complexity vulnerabilities.

Side Channel (SC) Definition
----------------------------
A challenge program contains a side channel vulnerability if and only if an
adversary can extract a worst-case complete secret value from the challenge
program with a defined probability of success by executing a specifically
bounded number of operations. This specific bound on the number of operations
is called the adversary's operational budget.

Operations include:  
        1. Provide the challenge program with one input and observe the
        challenge program's outputs.  
        2. Passively observe the challenge program's outputs without providing
        any input.  
        3. Query a notional oracle with a guess for the secret.  

Algorithmic Complexity (AC) Definition (V0)
-------------------------------------------
A Challenge program contains an algorithmic complexity vulnerability if an only
if it is possible for an adversary to cause that challenge program to exceed a
specific resource usage limit after feeding it some number of bytes of inputs,
where that number of bytes is less than a specific input budget.

Algorithmic Complexity (AC) Definition (V1)
-------------------------------------------
As an extension to AC Definition V0, in cases where the resource usage limit is
time, the following definitions are used to distinguish between self-DoS and
remote-DoS vulnerabilities.

        Self-DoS: A stand-alone challenge program contains an AC Time
        vulnerability if and only if a user can cause the application’s response
        time to exceed the resource usage limit while remaining within the input
        budget.

        Remote-DoS: A remote challenge program (server or peer-to-peer) contains
        an AC Time vulnerability if and only if it is possible for an adversary
        to cause a benign user’s request to exceed the resource usage limit
        while remaining within the input budget. Resource usage is evaluated for
        a reference benign user request (specified in the challenge question)
        rather than for any possible benign user interaction.

Acknowledgement
===============
The Apogee Research effort on the STAC program and by extension the work in this
repository is supported by the United States Air Force Research Lab and DARPA
under Contract Number FA8750-15-C-0089. THE VIEWS AND CONCLUSIONS CONTAINED IN 
THIS DOCUMENT ARE THOSE OF THE AUTHORS AND SHOULD NOT BE INTERPRETED AS 
REPRESENTING THE OFFICIAL POLICIES, EITHER EXPRESSED OR IMPLIED, OF THE DEFENSE 
ADVANCED RESEARCH PROJECTS AGENCY OR THE US GOVERNMENT.

License Information
===================
The work in this repository constitutes work by different companies as such the
license files associated with different applications in this repository may
differ. The license file for the Canonical Examples is contained within the
referenced folder. Each engagement challenge has its own license file.

