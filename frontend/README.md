# Frontend — MédiLabo Solutions

Interface Angular de l'application de dépistage du risque de diabète.
Générée avec [Angular CLI](https://github.com/angular/angular-cli) 22.0.2, et servie en
production par nginx (voir `Dockerfile`).

## Prérequis

- **Node.js ≥ 22** — l'Angular CLI 22 exige `v22.22.3+`, `v24.15.0+` ou `v26+`.
- Installer les dépendances du projet :

  ```bash
  npm install
  ```

  Cela installe `@angular/cli` **en local** (`node_modules/.bin/ng`). Aucune installation
  globale de `ng` n'est nécessaire — elle est même déconseillée, car sa version pourrait
  diverger de celle épinglée par le projet (source de « ça marche chez moi »).

> **Pourquoi les commandes ci-dessous n'appellent pas `ng` directement ?**
> Un `ng` nu n'existe sur le `PATH` que si l'Angular CLI est installé globalement. Deux
> mécanismes rendent le projet portable sans ce prérequis :
>
> - `npm run <script>` ajoute automatiquement `node_modules/.bin` au `PATH` le temps du
>   script → le `ng` **local** est résolu.
> - `npx ng <cmd>` cherche d'abord le binaire local du projet (version épinglée) avant de
>   retomber sur une éventuelle install globale.

## Commandes de développement

| Template d'origine (`ng` global) |      Équivalent portable      |
| :------------------------------- | :---------------------------- |
| `ng serve`                       | `npm start`                   |
| `ng build`                       | `npm run build`               |
| `ng test`                        | `npm test`                    |
| `ng generate component x`        | `npx ng generate component x` |
| `ng e2e`                         | `npx ng e2e`                  |

## Build de production

`npm run build` compile l'application et stocke les artefacts dans `dist/frontend/browser/`
(optimisé : tree-shaking, minification, hashing des noms de fichiers).

En conteneur, ce build est exécuté **dans l'image** (`Dockerfile`, stage `build`) puis servi
par nginx qui proxifie aussi `/api` vers la gateway.

> ⚠️ Après un `git pull` qui touche `frontend/`, la recompilation n'est **pas** automatique :
> il faut reconstruire l'image, sinon Docker ressert l'ancien bundle.
>
> ```bash
> docker compose up -d --build frontend
> ```

## Ressources

Référence complète des commandes de l'Angular CLI :
[Angular CLI Overview and Command Reference](https://angular.dev/tools/cli).
