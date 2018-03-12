
if [ -L privatekey_a.pem ]; then
    openssl rsa -in privatekey_a.pem -pubout > publickey_a.pub
else
    echo "Missing link to privatekey_a.pem"
    exit 1;
fi

if [ -L privatekey_b.pem ]; then
    openssl rsa -in privatekey_b.pem -pubout > publickey_b.pub
else
    echo "Missing link to privatekey_b.pem"
    exit 1;
fi

echo "Links generated in current directory"

