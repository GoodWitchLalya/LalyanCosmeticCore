### English

# Lalyan Cosmetic Core

An API to register and customize cosmetics without using armor slots

## Introduction

This plugin allows players to customize which cosmetic they have equipped, without having to use up their armor slots

It also allows plugin makers to create their custom cosmetics, without needing any java code!

The plugin features a custom GUI to customize cosmetics, which can be opened by command or, in a more immersive way, by interacting with any vanilla wardrobe

## Commands

The plugin has some commands that are mainly for testing purposes:

*   `/cosmetic apply <CosmeticId> [override]` manually applies a certain cosmetic defined by `CosmeticId`, `override` is optional, default is `yes [other value is no]`, determines whether all other cosmetics of its type should be removed. Requires OP

*   `/cosmetic change` manually opens the Cosmetic Customization UI. Does not require permissions

*   `/cosmetic list` prints in chat all loaded cosmetic ids. Requires OP

*   `/cosmetic reload` manually reloads all cosmetics. Requires OP

*   `/cosmetic reset` removes all custom cosmetics and resets the default skin. Requires OP


## Registering cosmetics

Registering a cosmetic is quite simple. You'll need to make an asset pack (obviously), and will need 3 files:

*   The cosmetic's .blockymodel
*   The cosmetic's .png texture
*   The cosmetic's .png icon, which is the same icon you would use for the item!

These files must be named in the same way, that being Cosmetic\_Id.extension

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

So, for a cosmetic called Custom\_Cape, which is in the cape slot it would be like this:

`Common/Resources/Cosmetics/Capes/Custom_Cape` and inside the Custom\_Cape folder:

```
Custom_Cape/
├── Custom_Cape.blockymodel
├── Custom_Cape.png
└── Icon/
    └── Custom_Cape.png
```



### Italiano

# Lalyan Cosmetic Core


Un'API per registrare e personalizzare cosmetici senza usare gli slot dell'armatura

## Introduzione

Questo plugin permette ai giocatori di personalizzare quale cosmetico hanno equipaggiato, senza dover usare i loro slot dell'armatura

Permette anche ai creatori di plugin di creare i loro cosmetici personalizzati, senza aver bisogno di alcun codice Java!

Il plugin presenta una GUI personalizzata per personalizzare i cosmetici, che può essere aperta tramite comando o, in un modo più immersivo, interagendo con qualsiasi armadio vanilla

## Comandi

Il plugin ha alcuni comandi che sono principalmente a scopo di test:

*   `/cosmetic apply <CosmeticId> [override]` applica manualmente un certo cosmetico definito da `CosmeticId`, `override` è opzionale, il valore predefinito è `yes [qualsiasi altro valore è no]`, determina se tutti gli altri cosmetici del suo tipo devono essere rimossi. Richiede OP

*   `/cosmetic change` apre manualmente l'interfaccia utente di personalizzazione dei cosmetici. Non richiede permessi

*   `/cosmetic list` stampa in chat tutti gli ID dei cosmetici caricati. Richiede OP

*   `/cosmetic reload` ricarica manualmente tutti i cosmetici. Richiede OP

*   `/cosmetic reset` rimuove tutti i cosmetici personalizzati e ripristina la skin predefinita. Richiede OP


## Registrazione dei cosmetici

Registrare un cosmetico è abbastanza semplice. Dovrai creare un pacchetto di risorse (ovviamente), e avrai bisogno di 3 file:

*   Il .blockymodel del cosmetico
*   La texture .png del cosmetico
*   L'icona .png del cosmetico, che è la stessa icona che useresti per l'oggetto!

Questi file devono essere nominati allo stesso modo, ovvero Cosmetic\_Id.extension

Ogni cosmetico avrà la sua cartella, con questa struttura

```
Custom_Id/
├── Custom_Id.blockymodel
├── Custom_Id.png
└── Icon/
    └── Custom_Id.png
```

Quindi il cosmetico andrà in una certa cartella, a seconda del tipo:

Il percorso base è `Common/Resources/Cosmetics`, quindi la cartella basata sullo slot, che può essere:

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

Quindi la cartella che hai creato prima.

Quindi, per un cosmetico chiamato Custom\_Cape, che si trova nello slot del mantello, sarebbe così:

`Common/Resources/Cosmetics/Capes/Custom_Cape` e all'interno della cartella Custom\_Cape:

```
Custom_Cape/
├── Custom_Cape.blockymodel
├── Custom_Cape.png
└── Icon/
    └── Custom_Cape.png
```
