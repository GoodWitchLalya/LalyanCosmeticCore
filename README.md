# Lalyan Cosmetic Core

An API to register and customize cosmetics without using armor slots

> **⚠️ Warning: Early Access**    
> The game Hytale is in early access, and so is this project! Features may be
> incomplete, unstable, or change frequently. Please be patient and understanding as development
> continues.

## Introduction
This plugin allows players to customize which cosmetic they have equipped, without having to use up their armor slots

It also allows plugin makers to create their custom cosmetics, without needing any java code!

The plugin features a custom GUI to customize cosmetics, which can be opened by command or, in a more immersive way, by interacting with any vanilla wardrobe

## Commands

The plugin has some commands that are mainly for testing purposes:

- `/cosmetic apply <CosmeticId> [override]` manually applies a certain cosmetic defined by `CosmeticId`, `override` is optional, default is `yes [other value is no]`, determines whether all other cosmetics of its type should be removed. Requires OP

- `/cosmetic change` manually opens the Cosmetic Customization UI. Does not require permissions

- `/cosmetic list` prints in chat all loaded cosmetic ids. Requires OP

- `/cosmetic reload` manually reloads all cosmetics. Requires OP

- `/cosmetic reset` removes all custom cosmetics and resets the default skin. Requires OP

## Registering cosmetics

Registering a cosmetic is quite simple. You'll need to make an asset pack (obviously), and will need 3 files:

- The cosmetic's .blockymodel
- The cosmetic's .png texture
- The cosmetic's .png icon, which is the same icon you would use for the item!

These files must be named in the same way, that being Cosmetic_Id.extension

Each cosmetic will be its own folder, with this structure

```
Custom_Id/
├── Custom_Id.blockymodel
├── Custom_Id.png
└── Icon/
    └── Custom_Id.png
```

Then the cosmetic will go in a certain folder, depending on the type:

The base path is `Common/Resources/Cosmetics`, then the folder based on the slot, which can be:
- `Capes`
- `Ears_Accessories`
- `Face_Accessories`
- `Gloves`
- `Head`
- `Overpants`
- `Overtops`
- `Pants`
- `Shoes`
- `Undertops`
- `Underwears`

Then the folder you made before.

So, for a cosmetic called Custom_Cape, which is in the cape slot it would be like this:

`Common/Resources/Cosmetics/Capes/Custom_Cape` and inside the Custom_Cape folder:

```
Custom_Cape/
├── Custom_Cape.blockymodel
├── Custom_Cape.png
└── Icon/
    └── Custom_Cape.png
```