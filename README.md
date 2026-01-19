### English

# Lalyan Cosmetic Core

An API to register and customize cosmetics without using armor slots

## Introduction

This plugin allows players to customize which cosmetic they have equipped, without having to use up their armor slots

It also allows plugin makers to create their custom cosmetics, without needing any java code!

The plugin features a custom GUI to customize cosmetics, which can be opened by command or, in a more immersive way, by interacting with any vanilla wardrobe

The GUI provides an easy way to select cosmetics and cosmetic variants. Left-click on a cosmetic to equip it, if the cosmetic has the variant icon, right-click it to open the variant menu!

## Commands

The plugin has some commands that are mainly for testing purposes:

*   `/cosmetic apply <CosmeticId> [override]` manually applies a certain cosmetic defined by `CosmeticId`, `override` is optional, default is `yes [other value is no]`, determines whether all other cosmetics of its type should be removed. Requires OP

*   `/cosmetic change` manually opens the Cosmetic Customization UI. Does not require permissions

*   `/cosmetic list` prints in chat all loaded cosmetic ids. Requires OP

*   `/cosmetic reload` manually reloads all cosmetics. Requires OP

*   `/cosmetic clear` removes all custom cosmetics and resets the default skin. Requires OP


## Registering cosmetics

Registering a cosmetic is quite simple. You'll need to make an asset pack (obviously), and will need 3 files:

*   The cosmetic's .blockymodel
*   The cosmetic's .png texture
*   The cosmetic's .png icon, which is the same icon you would use for the item!

These files must be named in the same way, that being Custom_ID.extension

Each cosmetic will be its own folder, with this structure

```
Custom_ID/
├── Custom_ID.blockymodel
├── Custom_ID.png
└── Icon/
    └── Custom_ID.png
```

Then the cosmetic will go in a certain folder, depending on the type:

### Cosmetics
(things like shirts, caps, pants etc..)

The base path for cosmetics is `Common/Resources/Cosmetics`, then the folder based on the slot, which can be:

*   `Capes`
*   `Ears_Accessories`
*   `Face_Accessories`
*   `Gloves`
*   `Head`
*   `Overpants`
*   `Overtops`
*   `Pants`
*   `Shoes`
*   `Undertops`
*   `Underwears`

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

### Characters
(things like mouth, ears, eyes etc..)

The base path for character cosmetics is `Common/Resources/Characters`, then the folder based on the slot, which can be:

*   `Eyes`
*   `Mouth`
*   `Nose`
*   `Eyebrows`
*   `Hair`
*   `Beard`
*   `Mustache`

Then the folder you made before.

So, for a character cosmetic called Custom_Eyes, which is in the cape slot it would be like this:

`Common/Resources/Cosmetics/Capes/Custom_Eyes` and inside the Custom_Eyes folder:

```
Custom_Eyes/
├── Custom_Eyes.blockymodel
├── Custom_Eyes.png
└── Icon/
    └── Custom_Eyes.png
```


## Advanced Cosmetics (optional)

Inside your cosmetic folder, you can add a `Cosmetic_Id.json` file. This file allows you to customize even more your cosmetics.

Within the JSON file, you are able to change the name and path of the model, texture and icon of your cosmetic. And that's not all, you'll also be able to define VARIANTS of your cosmetic and if the cosmetic occupies more than one slot!

```json
{
    "model": "Resources/Cosmetics/Head/Propeller_Hat/Propeller_Hat.blockymodel",
    "texture": "Resources/Cosmetics/Head/Propeller_Hat/Propeller_Hat.png",
    "icon": "Resources/Cosmetics/Head/Propeller_Hat/Propeller_Hat_Icon.png",
    "variants": {
        "Variant": {
            "texture": "Resources/Cosmetics/Head/Propeller_Hat/Variant/Variant.png",
            "icon": "Resources/Cosmetics/Head/Propeller_Hat/Variant/Variant_Icon.png"
        }
    }
}
```


### Italiano

# Lalyan Cosmetic Core

Un'API per registrare e personalizzare cosmetici senza usare gli slot dell'armatura

## Introduzione

Questo plugin permette ai giocatori di personalizzare quale cosmetico hanno equipaggiato, senza dover usare i loro slot dell'armatura

Permette anche ai creatori di plugin di creare i loro cosmetici personalizzati, senza aver bisogno di alcun codice Java!

Il plugin fornisce una GUI per personalizzare i cosmetici, che può essere aperta tramite comando o, in un modo più immersivo, interagendo con qualsiasi armadio vanilla

La GUI fornisce un modo semplice per selezionare cosmetici e varianti di cosmetici. Fai clic con il pulsante sinistro su un cosmetico per equipaggiarlo, se il cosmetico ha l'icona della variante, fai clic con il pulsante destro per aprire il menu delle varianti!

## Comandi

Il plugin ha alcuni comandi che sono principalmente a scopo di test:

*   `/cosmetic apply <CosmeticId> [override]` applica manualmente un certo cosmetico definito da `CosmeticId`, `override` è opzionale, il valore predefinito è `yes [l'altro valore è no]`, determina se tutti gli altri cosmetici del suo tipo devono essere rimossi. Richiede OP

*   `/cosmetic change` apre manualmente l'interfaccia utente di personalizzazione dei cosmetici. Non richiede permessi

*   `/cosmetic list` stampa in chat tutti gli ID dei cosmetici caricati. Richiede OP

*   `/cosmetic reload` ricarica manualmente tutti i cosmetici. Richiede OP

*   `/cosmetic clear` rimuove tutti i cosmetici personalizzati e ripristina la skin predefinita. Richiede OP


## Registrazione dei cosmetici

Registrare un cosmetico è abbastanza semplice. Dovrai creare un pacchetto di risorse (ovviamente), e avrai bisogno di 3 file essenziali:

*   Il file .blockymodel del cosmetico
*   La texture .png del cosmetico
*   L'icona .png del cosmetico, che è la stessa icona che useresti per l'oggetto!

Questi file devono essere nominati allo stesso modo, ovvero Custom_ID.extension

Ogni cosmetico avrà la sua cartella, con questa struttura

```
Custom_ID/
├── Custom_ID.blockymodel
├── Custom_ID.png
└── Icon/
    └── Custom_ID.png
```

La cartella del cosmetico andrà messa all'interno di una certa cartella, a seconda del tipo di cosmetico:

### Cosmetici
(cose come magliette, cappelli, pantaloni ecc..)

Il percorso base per i cosmetici è `Common/Resources/Cosmetics`, seguito dalla cartella basata sullo slot, che può essere:

*   `Capes`
*   `Ears_Accessories`
*   `Face_Accessories`
*   `Gloves`
*   `Head`
*   `Overpants`
*   `Overtops`
*   `Pants`
*   `Shoes`
*   `Undertops`
*   `Underwears`

Scegli quale di questi slots deve occupare il tuo cosmetico e inserisci la cartella che hai creato prima.

Quindi, per un cosmetico chiamato `Custom_Cape`, che si trova nello slot `Capes`, il percorso sarà questo:

`Common/Resources/Cosmetics/Capes/Custom_Cape`, e all'interno della cartella `Custom_Cape`:

```
Custom_Cape/
├── Custom_Cape.blockymodel
├── Custom_Cape.png
└── Icon/
    └── Custom_Cape.png
```

### Parti del Personaggio
(cose come bocca, orecchie, occhi ecc..)

Il percorso base per i cosmetici del personaggio è `Common/Resources/Characters`, seguito dalla cartella basata sullo slot, che può essere:

*   `Eyes`
*   `Mouth`
*   `Nose`
*   `Eyebrows`
*   `Hair`
*   `Beard`
*   `Mustache`

Scegli quale di questi slots deve occupare la tua parte per il personaggio e inserisci la cartella che hai creato prima.

Quindi, per un cosmetico del personaggio chiamato `Custom_Eyes`, che si trova nello slot `Eyes`, il percorso sarà questo:

`Common/Resources/Characters/Eyes/Custom_Eyes`, e all'interno della cartella `Custom_Eyes`:

```
Custom_Eyes/
├── Custom_Eyes.blockymodel
├── Custom_Eyes.png
└── Icon/
    └── Custom_Eyes.png
```


## Cosmetici Avanzati (opzionale)

All'interno della cartella del tuo cosmetico, puoi aggiungere un file `Custom_Id.json` (in cui Custom_Id è l'ID del tuo cosmetico).
Questo file ti permetterà di personalizzare ancora di più i tuoi cosmetici.

All'interno del file JSON puoi cambiare il nome e il percorso del modello, della texture e dell'icona del tuo cosmetico. E non è tutto, potrai anche definire VARIANTI del tuo cosmetico e decidere se occupa slot multipli!

Qui la formattazione corretta del cosmetico di esempio

```json
{
  "model": "Resources/Cosmetics/Head/Propeller_Hat/Propeller_Hat.blockymodel",
  "texture": "Resources/Cosmetics/Head/Propeller_Hat/Propeller_Hat.png",
  "icon": "Resources/Cosmetics/Head/Propeller_Hat/Propeller_Hat_Icon.png",
  "variants": {
    "Variant1": {
      "texture": "Resources/Cosmetics/Head/Propeller_Hat/Variant/Variant1.png",
      "icon": "Resources/Cosmetics/Head/Propeller_Hat/Variant/Variant1_Icon.png"
    },
    "Variant2": {
      "texture": "Resources/Cosmetics/Head/Propeller_Hat/Variant/Variant2.png",
      "icon": "Resources/Cosmetics/Head/Propeller_Hat/Variant/Variant2_Icon.png"
    }
  },
  "slot_overrides": [
    "Capes"
  ]
}
```