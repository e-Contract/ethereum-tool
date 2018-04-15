Ethereum Tool
=============

Command line tool for offline template-based transaction signing.

If you like this tool, please consider a donation at:
```
0x0c56073db91c2Ba57FF362301eb32262BBeE6147
```


# Installation

Download the latest ZIP from:
https://www.e-contract.be/maven2/be/e-contract/ethereum-tool/

Install (Mac OS X or Linux) via:
```
sudo unzip -o ethereum-tool-1.0.0.zip -d /usr/local/
```


# Usage

Run the tool via:
```
ethereum-tool
```


# Offline key management

Reboot your machine using a Linux Live USB stick so you work in a clean and secure environment.

The `password` command might give you some inspiration for a strong password.
```
ethereum-tool password
```

Create a new key and corresponding transaction template via:
```
ethereum-tool createkey -d keystore -t templates
```

The `keystore` directory will contain the new key.
The `templates` directory will contain the corresponding transaction template.

Store the key in a save way (e.g., multiple USB keys or so).
The transaction template can be used to prepare transactions to be signed.

Verify the key via:
```
ethereum-tool verifykey -f keystore/UTC--...
```

You can also create a transaction template from an existing key via:
```
ethereum-tool verifykey -f keystore/UTC--... -t templates
```


# Preparing a transaction

Preparing a transaction requires an online machine.

Copy the transaction template to a `transaction.json` file to prepare a new transaction.

You need to change some fields within the `transaction.json` file.

First of all the nonce need to be set correct.
Retrieve the correct nonce of your address via:
```
ethereum-tool nonce -l http://localhost:8545 -a your_address_here
```
This of course requires a running and synched Ethereum node.

You also might want to adjust the gas price.
Query the current gas price (as reported by your local node) via:
```
ethereum-tool gasprice -l http://localhost:8545
```

Notice that this might not be the sharpest gas price possible.
Check out the network speed to see the currently used gas prices:
```
ethereum-tool speed -l http://localhost:8545
```


# Offline transaction signing

After the transaction JSON file has been prepared we boot again via our Linux Live USB to have a clean and secure environment.

Sign the transaction via:
```
ethereum-tool sign -f keystore/UTC--... -t transaction.json -o transaction
```

Inspect the signed transaction via:
```
ethereum-tool inspect -f transaction
```

# Transmitting the transaction

This requires an online machine of course.

Transmit the transaction to the network via:
```
ethereum-tool transmit -l http://localhost:8545 -f transaction
```

Check the status of the transaction via:
```
ethereum-tool confirm -l http://localhost:8545 -f transaction
```


# Development

Build the project via Maven:
```
mvn clean install
```

We use Netbeans as IDE.
If you send pull requests, please keep the code clean to ease the review process.
