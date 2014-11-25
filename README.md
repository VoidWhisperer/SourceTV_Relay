<h2>SourceTV_Relay</h2>

An application to help faciliate the dynamic creation and destruction of SourceTV relays.

<h4>How to use</h4>
Essentially you compile this into a JAR file, which can be done by compiling the code as an artifact in almost any IDE. Then, you run the jar file.*
This will launch a web server on the port that you defined in the config, and then you can do the following:

http://ip:port/createRelay/?key=key&ip=ip&port=port

This will create a relay on the port. If it's succesful it will return something like:

{"success","ip:port"}

the IP and port is the ip and port of the relay. If it has an error it will return

{"error":"[Error message]"}

Now to destroy relays:

http://ip:port/destroyRelay/?key=key&ip=ip&port=port

This will destroy the relay that was running for that server (where ip and port are the ip and port of that you used to make the relay). If it's succesful it will return something like:

{"success","Server destroyed"}

If it has an error it will return

{"error":"[Error message]"}

*At the moment this only works on linux due to how it kills the and starts the processes.

Any pull requests are welcome!
