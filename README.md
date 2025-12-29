# Minecraft Bedrock Parity Plugin

A modest plugin to bring a few Minecraft Bedrock features to Minecraft Java.

This plugin was initially written for SpigotMC version 1.20.4, and is available for versions up to 1.21.11.

## Installing

You need JDK version 21 and Apache Maven.

Run `mvn package` to build the plugin JAR in the `target` folder.

## Features

* Salmon will drop Bone instead of Bone Meal.
* Players can apply Bone Meal to the following items in the world in order to duplicate them:
  * Dandelion
  * Poppy
  * Blue Orchid
  * Allium
  * Azure Bluet
  * Red Tulip
  * Orange Tulip
  * White Tulip
  * Pink Tulip
  * Oxeye Daisy
  * Cornflower
  * Lily Of The Valley
  * Closed Eyeblossom
  * Open Eyeblossom
  * Sugar Cane
* Each flower duplicated from a Dandelion or Poppy has a 15% chance of being a Poppy or Dandelion, respectively.

## Sugar cane generation

Replaces 1-2 Air blocks with Sugar Cane blocks above the one to which the player has applied Bone Meal. There must be at
least one Air block above the Sugar Cane block.

Minecraft Bedrock behaves differently depending on which face of the Sugar Cane block the Bone Meal is applied to. For
the sake of simplicity I have not attempted to replicate this behaviour.

## Flower generation

Makes up to 32 attempts to find a suitable nearby location where it can duplicate the flower. For a location to be
suitable, it must contain a Grass block with an Air block above it.

The function to find a nearby location makes three random moves starting at the flower's location.
  * Each move has an equal (1/3) chance of moving either -1, 0 or 1 blocks in each of the X and Z directions.
  * Each move has an 8/10 chance of remaining at the same Y level.
  * Each move has a 1/10 chance of moving either -1 or 1 spaces in the Y direction.

This results in the following probability distribution for possible nearby locations, offset from the flower's location
(probabilities shown are percentage chances of occurrence):

### Y = 0

| Z/X | 0      | ±1     | ±2     | ±3     |
|-----|--------|--------|--------|--------|
| 0   | 3.7640 | 3.2263 | 1.6131 | 0.5377 |
| ±1  | 3.2263 | 2.7654 | 1.3827 | 0.4609 |
| ±2  | 1.6131 | 1.3827 | 0.6913 | 0.2304 |
| ±3  | 0.5377 | 0.4609 | 0.2304 | 0.0768 |

### Y = ±1

| Z/X | 0      | ±1     | ±2     | ±3     |
|-----|--------|--------|--------|--------|
| 0   | 1.3106 | 1.1234 | 0.5617 | 0.1872 |
| ±1  | 1.1234 | 0.9629 | 0.4814 | 0.1604 |
| ±2  | 0.5617 | 0.4814 | 0.2407 | 0.0802 |
| ±3  | 0.1872 | 0.1604 | 0.0802 | 0.0267 |

### Y = ±2

| Z/X | 0      | ±1     | ±2     | ±3     |
|-----|--------|--------|--------|--------|
| 0   | 0.1613 | 0.1382 | 0.0691 | 0.0230 |
| ±1  | 0.1382 | 0.1185 | 0.0592 | 0.0197 |
| ±2  | 0.0691 | 0.0592 | 0.0296 | 0.0098 |
| ±3  | 0.0230 | 0.0197 | 0.0098 | 0.0032 |

### Y = ±3

| Z/X | 0      | ±1     | ±2     | ±3     |
|-----|--------|--------|--------|--------|
| 0   | 0.0067 | 0.0057 | 0.0028 | 0.0009 |
| ±1  | 0.0057 | 0.0049 | 0.0024 | 0.0008 |
| ±2  | 0.0028 | 0.0024 | 0.0012 | 0.0004 |
| ±3  | 0.0009 | 0.0008 | 0.0004 | 0.0001 |

If the nearby location is unsuitable, the attempt fails and a flower is not duplicated at that location. The number of
flowers actually generated will depend on the terrain and placement of the original flower.

The flower generation process here is a "best estimate" based on some fairly crude statistical observations.

The distribution of generated flowers, and the proportions of generated Poppy and Daffodil flowers produced when
applying Bone Meal to the other type of flower, may not precisely match those in Bedrock.
