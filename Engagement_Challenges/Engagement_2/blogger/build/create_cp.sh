BUILD=$(cd "$( dirname "${BASH_SOURCE[0]}")/." && pwd)
SOURCE=$( cd "$( dirname "${BASH_SOURCE[0]}" )"/../source && pwd )
BTCPDIR=$(cd "$( dirname "${BASH_SOURCE[0]}" )"/../../BT/challenge_program && pwd )

echo "Using build dir: ${BUILD}"
echo "Using source dir: ${SOURCE}"
echo "Using blue team challenge program dir: ${BTCPDIR}"

pushd "$SOURCE" &>/dev/null
if [ ! $? ]; then
    popd &>/dev/null
    echo "ERROR: $SOURCE directory is not ready"
    exit -1
fi

if [ ! -e "./pom.xml" ]; then
    echo "Project Object Model not found... Are you in the correct directory?"
    popd &>/dev/null
    exit -1;
fi

if (grep -e "<artifactId>nanohttpd-project</artifactId>" "./pom.xml" &> /dev/null); then
    rm -rf target/archive
    echo "Building... Please Wait"
    if (mvn package &> '.create_cp_result'); then
        rm -f '.create_cp_result' &> /dev/null
        mkdir -p target/archive/content
        #cp "javawebapplication/target/nanohttpd-javawebapplication-2.2.0-SNAPSHOT.jar" target/archive/content &>/dev/null
        cp "javawebserver/target/nanohttpd-javawebserver-2.2.0-SNAPSHOT-jar-with-dependencies.jar" target/archive/content &>/dev/null
        pushd target/archive/content &>/dev/null
        cat > run.sh <<EOF
#!/bin/sh

java -Xint -jar nanohttpd-javawebserver-2.2.0-SNAPSHOT-jar-with-dependencies.jar
EOF
        cat > example-input.sh <<EOF
#!/bin/sh

curl -i http://localhost:8080/stac/example/Example
EOF
        chmod 755 run.sh example-input.sh
        tar -cf ../challenge_program.tar .
        popd &>/dev/null
        popd &>/dev/null
        cp "$SOURCE/target/archive/challenge_program.tar" ${BTCPDIR}/.
        pushd "$SOURCE" &>/dev/null
        mvn clean &>/dev/null
        rm -rf ./target &>/dev/null
        popd &>/dev/null
    else
        echo "Build failed."
        cat '.create_cp_result'
        rm -f '.create_cp_result' &> /dev/null
        popd &>/dev/null
        exit -1
    fi
fi

popd &>/dev/null
