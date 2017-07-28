#/bin/bash

#MIT License
#
#Copyright (c) 2017 Apogee Research
#
#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:
#
#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.
#
#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.

# Create Centos Vault repository file
cat > CentosVault.repo << EOF
[centosvaultrepo]
name=Centos Vault Repository
baseurl=http://vault.centos.org/7.1.1503/updates/x86_64
enabled=1
gpgcheck=0
EOF


# Create STAC Base Image (Centos 7 + OpenJDK) and push it to Registry
cat > Dockerfile << EOF
FROM centos:7.1.1503
RUN /bin/echo "LANG=\"en_US.UTF-8\"" > /etc/profile.d/lang.sh
ADD CentosVault.repo /etc/yum.repos.d/CentosVault.repo
RUN yum -y install java-1.8.0-openjdk-1:1.8.0.65-2.b17.el7_1.x86_64
EOF
docker build -t stac_base:v6 .

