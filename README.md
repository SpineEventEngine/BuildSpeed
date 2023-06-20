# BuildPerformance

This repository contains a set of Protobuf model definitions.

Unlike many other [Spine examples](https://github.com/spine-examples), this repo does not contain
Java or any other implementations. Instead, it focuses on the model definitions, providing
a sizable amount of various Proto definitions.

Using the definitions in this repo, we can test performance of our build tools, comparing it to
the past performance.

The repo contains 1152 message declarations and 32 enum declarations in 192 `.proto` files.
This includes command, event, rejection, entity, ID, and value object types.
The declarations are split into several identical packages which only differ in names.
This allows us to have more data for testing build tool performance.
