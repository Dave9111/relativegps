RELATIVE GPS
===========

Relative GPS is a project aimed at providing high-precision, relative tracking information for a scalable network of mobile GPS receivers. It is based on the algorithms and methodology described in the paper **High Accuracy Differential Tracking of Low-Cost GPS Receivers** by *Hedgecock et al.*

The project is implemented as a Java-based library and is not intended to be used as a standalone application. See the Installation and Usage sections for more information on how to set up this library for inclusion in your own project.

If you plan to distribute derivative or standalone works which include any part of the source code provided here, please refer to the LICENSE document for additional information.

Installation
-----------

Installation of this library is as simple as downloading a copy of the code and including it in the source tree for your own project. For example in Eclipse, you would `Import` this directory as an existing project into your workspace. Then, in the `Project References` page of your project's property info, make sure that the `relativegps` checkbox is marked.

Note that this library also relies on a bundled library called the `MessagingFramework` which can be found in the `libs` directory. You must ensure that the project is able to find this library to avoid compile errors. Again, in Eclipse, this entails adding the `MessagingFramework.jar` archive to the list of libraries in the build path for this project.

Usage
-----------

In order to use this framework, you will need to define three separate interface modules:

  1. A `SerialInterface` which will be used to provide GPS data packets in the UBX format to the framework,
  2. A `NetworkInterface` which will be used to broadcast and receive byte streams over a network, and
  3. An `OutputInterface` which will be used to handle the tracking outputs produced by the framework.

Each `Interface` you provide will need to extend the `RLInterfaceImplementation` class and override the following three methods:

  * `void openInterface()` which will provide any code necessary to open/initialize your interface,
  * `void closeInterface()` which will provide any code necessary to gracefully close/shutdown your interface, and
  * `void handleMessageFromFramework(final RLMessage message)` which will handle any messages you may receive from the framework.

Currently, the `SerialInterface` will never receive any messages that must be handled, the `NetworkInterface` may receive a `ByteStream` message that must be broadcast to the entire network, and the `OutputInterface` may receive a `Result` message containing the relative tracking results for the current epoch from the framework. Additionally, the `SerialInterface` will need to provide a `ByteStream` message containing a byte array of the raw UBX packet to the framework by calling `sendMessageToFramework(new RLMessage(new ByteStream(dataPacket)), false)`, where `dataPacket` is a `byte[]`, and the `NetworkInterface` will need to pass any network packets it receives to the framework by calling `sendMessageToFramework(new RLMessage(new ByteStream(packetData)))`, where `packetData` is a `byte[]` containing the received packet.

Once your interfaces are all created, you can set up the entire framework by calling the following functions:

  * `RegLocFramework framework = new RegLocFramework([Your Receiver ID Here]);`
  * `framework.connectInterfaceToImplementation("NetworkInterface", new [Your NetworkInterface Here]);`
  * `framework.connectInterfaceToImplementation("SerialInterface", new [Your SerialInterface Here]);`
  * `framework.connectInterfaceToImplementation("OutputInterface", new [Your OutputInterface Here]);`

Then, to start the framework, you simply call `framework.startProcessing()`.  Likewise, to stop the framework, you can call `framework.stopProcessing()`.

Web Site
-----------

For the most up-to-date information regarding the techniques and methodology used in this library implementation, please visit the [Relative GPS Project Web Site](http://www.isis.vanderbilt.edu/projects/relativeGPS).

Notice
-----------

Relative GPS contains source code that is Copyright (C) 2014 Will Hedgecock. It is distributed under the GNU Affero GPL license. Refer to the LICENSE document for more information about using this library in your own project.
