FROM trie-challenge

RUN yum -y install numpy

RUN useradd mal; chmod 755 /home/mal; \
    ln -s /home/subspace/scripts /home/mal/scripts; \
    echo mal > /usr/local/etc/login-user

COPY \
     register-exploit-user.sh \
     exploit.py \
     util.py \
     get_thresholds.py \
     /home/mal/

RUN chown -R mal:mal /home/mal
