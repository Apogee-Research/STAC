#! /bin/bash

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

USAGE="sudo $0 ([centos] [docker]) | all"

if [[ $EUID -ne 0 ]]; then echo "ERROR: You must run $0 with sudo"; exit -1; fi
if [ $# -eq 0 ]; then echo $USAGE; exit -1; fi

CONFIG_CENTOS=0
INSTALL_DOCKER=0

while [ $# -ne 0 ]
do
    case "$1" in
        centos)
            CONFIG_CENTOS=1
            ;;
        docker)
            INSTALL_DOCKER=1
            ;;
        all)
            CONFIG_CENTOS=1
            INSTALL_DOCKER=1
            ;;
        *)
            echo $USAGE
            exit -1
    esac
    shift
done

function configCentos() {
    systemctl disable tuned.service
    systemctl stop tuned.service

    systemctl disable crond.service
    systemctl stop crond.service

    systemctl disable NetworkManager.service
    systemctl stop NetworkManager.service

    systemctl disable firewalld.service
    systemctl stop firewalld.service

    systemctl disable postfix.service
    systemctl stop postfix.service

    systemctl enable cpupower.service
    systemctl start cpupower.service

    sysctl -w net.bridge.bridge-nf-call-iptables="1"
    sysctl -w net.bridge.bridge-nf-call-ip6tables="1"

    cat >>/etc/sysconfig/network-scripts/ifcfg-lo <<EOF
MTU="1500"
EOF
}

function installDocker() {
    # Create Partitions for Docker images
    # /dev/sda4 for Docker images
    # /dev/sda5 for Hot Partition should this be required


    echo "Creating Partition for Docker Image Storage"

    PT_TYPE=`parted -l 2>/dev/null | grep "Partition Table:" | head -1 | sed 's/Partition Table:\s//'`
    if [ "$PT_TYPE" != "gpt" ]; then echo "ERROR: Wrong Partition table type"; exit -1; fi

    if [ -e "/dev/sda4" ]; then echo "ERROR: /dev/sda4 already exists"; exit -1; fi
    if [ -e "/dev/sda5" ]; then echo "ERROR: /dev/sda5 already exists"; exit -1; fi

    fdisk /dev/sda <<EOF
n
4

+40G
n
5

+20G
w
EOF
    partprobe

    # Install Docker
    echo "Installing Docker"
    cat >/etc/yum.repos.d/docker.repo <<EOF
[dockerrepo]
name=Docker Repository
baseurl=https://yum.dockerproject.org/repo/main/centos/7
enabled=1
gpgcheck=1
gpgkey=https://yum.dockerproject.org/gpg
EOF
    yum install -y docker-engine-1.9.1-1.el7.centos

    # Add current user to Docker group - to avoid having to sudo docker
    echo "Add stac user to docker group"
    usermod -aG docker stac

    # Setup storage as device mapper - thin, direct
    echo "Creating volumes for Docker Image Storage"
    rm -rf /var/lib/docker
    pvcreate /dev/sda4
    vgcreate vg-docker /dev/sda4
    lvcreate -L 32G -n data vg-docker
    lvcreate -L 7.9G -n metadata vg-docker

    OPTIONS="--storage-driver=devicemapper --storage-opt dm.datadev=\/dev\/vg-docker\/data --storage-opt dm.metadatadev=\/dev\/vg-docker\/metadata"

    # Configure base filesystem as XFS
    OPTIONS="${OPTIONS} --storage-opt dm.fs=xfs"

    # Don't have Systemd control cgroups (triggers bug in Docker)
    OPTIONS="${OPTIONS} --exec-opt native.cgroupdriver=cgroupfs"

    # Apply OPTIONS
    echo "Setting docker startup options"
    sed -i -e "s/\(ExecStart.*\)/\1 ${OPTIONS}/" /lib/systemd/system/docker.service

    # Systemd setup
    echo "Setup Systemd for Docker"
    systemctl daemon-reload
    systemctl enable docker.service
    systemctl restart docker.service

    # Done
    echo "Please reboot to ensure all changes have taken effect"
}

if [ "$CONFIG_CENTOS" -eq 1 ]; then
    configCentos
fi

if [ "$INSTALL_DOCKER" -eq 1 ]; then
    installDocker
fi

echo "DONE"
