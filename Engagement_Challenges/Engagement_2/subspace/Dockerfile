FROM localhost:5000/stac_base:v5

# Replace fakesystemd with systemd, required by Postfix.
# From https://hub.docker.com/_/centos/
ENV container docker
RUN yum -y swap -- remove fakesystemd -- install systemd systemd-libs
#RUN yum -y update; yum clean all
RUN (cd /lib/systemd/system/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done); \
rm -f /lib/systemd/system/multi-user.target.wants/*; \
rm -f /etc/systemd/system/*.wants/*; \
rm -f /lib/systemd/system/local-fs.target.wants/*; \
rm -f /lib/systemd/system/sockets.target.wants/*udev*; \
rm -f /lib/systemd/system/sockets.target.wants/*initctl*; \
rm -f /lib/systemd/system/basic.target.wants/*; \
rm -f /lib/systemd/system/anaconda.target.wants/*;
VOLUME [ "/sys/fs/cgroup" ]
#CMD ["/usr/sbin/init"]

# Setup basic system components.
RUN ["yum", "-y", "install", "rsyslog"]
ADD challenge_program/data/rsyslog.conf /etc/rsyslog.conf

# Setup the mail system.
RUN ["yum", "-y", "install", "postfix"]
ADD challenge_program/data/postfix/ /etc/postfix/
RUN ["postmap", "/etc/postfix/transport"]

# Add stunnel for HTTPS support.
RUN ["yum", "-y", "install", "stunnel"]
ADD challenge_program/data/stunnel /etc/stunnel/
# Add mailx for minimal mail support.
RUN ["yum", "-y", "install", "mailx"]


# Basic configuration.
ENV APP_NAME=subspace APP_MAIN_CLASS=com.example.subspace.Main

# Setup the user who will run the app code.
ENV APP_USER ${APP_NAME}
ENV APP_GROUP ${APP_USER}
ENV APP_HOME /home/${APP_USER}
RUN groupadd -r "${APP_GROUP}"; \
    useradd -d "${APP_HOME}" -g "${APP_GROUP}" -m -N -r "${APP_USER}"
RUN chmod 755 "${APP_HOME}"

# Setup the user who will run the front end (web client and browser).
ENV FRONTEND_USER stac
ENV FRONTEND_GROUP ${FRONTEND_USER}
ENV FRONTEND_HOME /home/${FRONTEND_USER}
RUN groupadd "${FRONTEND_GROUP}"; \
    useradd -d "${FRONTEND_HOME}" -g "${FRONTEND_USER}" -m -N "${FRONTEND_USER}"
RUN chmod 755 "${FRONTEND_HOME}"

# Setup the app.
ENV APP_CONFIG_DIR ${APP_HOME}/config
ENV APP_CONFIG_FILE ${APP_CONFIG_DIR}/${APP_NAME}.properties
ENV APP_DATA_DIR ${APP_HOME}/data
ENV APP_LIB_DIR ${APP_HOME}/lib
ENV APP_JAR_DIR ${APP_HOME}/lib
ADD challenge_program/data/app-config/ ${APP_CONFIG_DIR}/
ADD challenge_program/data/app-data/ ${APP_DATA_DIR}/
ADD challenge_program ${APP_HOME}/

# Add required files/folders
ADD APIsUsed.txt $FRONTEND_HOME
ADD description.txt $FRONTEND_HOME
ADD examples $FRONTEND_HOME/examples
ADD questions $FRONTEND_HOME/questions
RUN chown -R "${FRONTEND_USER}:${FRONTEND_GROUP}" "${FRONTEND_HOME}"

# Setup stunnel.
ENV STUNNEL_CONFIG_DIR ${APP_HOME}/stunnel
ADD challenge_program/data/stunnel/ ${STUNNEL_CONFIG_DIR}/
RUN chown -R "${APP_USER}":"${APP_GROUP}" "${STUNNEL_CONFIG_DIR}" && \
    chmod 640 "${STUNNEL_CONFIG_DIR}"/stunnel.pem

# Chown everything.
RUN chown -R "${APP_USER}:${APP_GROUP}" "${APP_HOME}"

# Setup the graphical environment.
#ENV VNC_DISPLAY=:0 \
#    VNC_PORT=5900 \
#    VNC_PASS=password \
#    VNC_PASS_FILE=${FRONTEND_HOME}/.vncpass \
#    VNC_RESOLUTION=1024x768
#RUN ["yum", "-y", "install", "tigervnc-server", "motif"]
#RUN printf %s\\n "${VNC_PASS}" | vncpasswd -f > "${VNC_PASS_FILE}"; \
#    chown "${FRONTEND_USER}:${FRONTEND_GROUP}" "${VNC_PASS_FILE}"; \
#    chmod 700 "${VNC_PASS_FILE}"
#ADD challenge_program/data/Xclients ${FRONTEND_HOME}/.Xclients
#EXPOSE ${VNC_PORT}

# Setup the user apps to interact with the app.
#RUN ["yum", "-y", "install", "thunderbird", "firefox", "mailx", "mutt"]
#ADD challenge_program/data/thunderbird/local.js /usr/lib64/thunderbird/defaults/pref/
#ADD challenge_program/data/thunderbird/local.cfg /usr/lib64/thunderbird/

# Setup additional frontend apps.
RUN ["yum", "-y", "install", "mutt"]

# Disable DNS
RUN sed -i -e "s/\(^hosts:.*\)/#\1\nhosts: files/" /etc/nsswitch.conf

# Set the initial command to run.
ENV INIT_LOG_DIR /var/log/${APP_NAME}-init
RUN mkdir -p "${INIT_LOG_DIR}"
ADD challenge_program/data/init /usr/local/sbin/init
RUN chown root:subspace /usr/local/sbin/init; \
    chmod 740 /usr/local/sbin/init; \
    mkdir -p /usr/local/etc; \
    echo "${FRONTEND_USER}" > /usr/local/etc/login-user
CMD ["/usr/local/sbin/init"]
