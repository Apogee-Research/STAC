FROM localhost:5000/stac_base:v6
RUN yum makecache fast
RUN yum install -y wget
RUN yum install -y vim-enhanced.x86_64
RUN yum install -y tar
RUN yum install -y bzip2
RUN yum install tcpdump -y
WORKDIR /tmp
RUN wget https://repo.continuum.io/archive/Anaconda3-4.2.0-Linux-x86_64.sh
RUN chmod 755 Anaconda3-4.2.0-Linux-x86_64.sh
RUN HOME=/home && ./Anaconda3-4.2.0-Linux-x86_64.sh -b
WORKDIR /usr/local/bin/
RUN ln -s /home/anaconda3/bin/python3.5 ./python3.5
RUN /home/anaconda3/bin/pip install scapy-python3
ADD ["Category_1", "/home/Category_1"]
ADD ["Category_2", "/home/Category_2"]
ADD ["Category_3", "/home/Category_3"]
ADD ["Category_4", "/home/Category_4"]
ADD ["Category_5", "/home/Category_5"]
ADD ["Category_6", "/home/Category_6"]
ADD ["Category_7", "/home/Category_7"]
ADD ["Category_8", "/home/Category_8"]
ADD ["Category_9", "/home/Category_9"]
ADD ["Category_10", "/home/Category_10"]
ADD ["Category_11", "/home/Category_11"]
ADD ["Category_12", "/home/Category_12"]
ADD ["Category_13", "/home/Category_13"]
ADD ["Category_14", "/home/Category_14"]
ADD ["Category_15", "/home/Category_15"]
ADD ["Category_16", "/home/Category_16"]
WORKDIR /home/
