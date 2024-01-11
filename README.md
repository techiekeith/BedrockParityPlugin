# Bedrock Parity Plugin

A modest plugin to bring a few Minecraft Bedrock features to Minecraft Java.

This plugin was written for SpigotMC v1.20.4.

## Features

* Salmon will now drop Bone instead of Bone Meal.
* Players can now apply Bone Meal to the following items in the world:
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
  * Sugar Cane

## Differences with actual Bedrock behaviour

### Sugar cane generation

Bedrock behaves differently depending on which face of the Sugar Cane block the Bone Meal is applied to. For the sake
of simplicity I have not attempted to replicate this behaviour.

### Flower generation

The flower generation process here is a "best estimate" based on some fairly crude statistical observations.

The distribution of generated flowers, and the proportions of generated Poppy and Daffodil flowers produced when
applying Bone Meal to the other type of flower, may not precisely match those in Bedrock.
