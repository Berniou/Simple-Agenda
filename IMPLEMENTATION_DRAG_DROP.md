# Implémentation du Drag & Drop avec Réorganisation Automatique

## Vue d'ensemble
Les cartes de la timeline quotidienne sont maintenant **entièrement draggables** et s'**organisent automatiquement** lors des déplacements.

## Changements effectués

### 1. `DayTimelineView.java`

#### Ajouts à l'interface `BlockMoveListener`:
- `void onBlocksReordered(List<BlockReorderInfo> reordered)` - Callback pour notifier les changements d'arrangement

#### Nouvelle classe interne `BlockReorderInfo`:
```java
public static class BlockReorderInfo {
    public long scheduledId;
    public int newStartMinutesFromMidnight;
}
```
Contient les infos de réorganisation pour chaque bloc affecté.

#### Nouveau système de tracking avec `BlockInfo`:
- Classe interne pour stocker les infos des blocs (card, id, durée, position originale)
- `Map<Long, BlockInfo> blockMap` pour accéder rapidement à un bloc par son ID

#### Changements dans `setBlocks()`:
- Chaque **carte entière** est maintenant draggable (pas seulement la poignée)
- Les infos des blocs sont stockées dans `blockMap`

#### Mise à jour de `BlockDragHelper`:
- Accepte maintenant `durationMin` comme paramètre
- Lors du **ACTION_MOVE**: appelle `updateOtherBlocksPosition()` pour repositionner les blocs qui chevauchent
- Lors du **ACTION_UP**: appelle `reorganizeBlocks()` pour mettre à jour la base de données

#### Nouvelles méthodes:

##### `updateOtherBlocksPosition()`:
- Vérifie les chevauchements en temps réel pendant le drag
- Déplace les blocs qui chevauchent vers le haut ou le bas automatiquement
- Respecte les bornes de la journée (6h–22h)

##### `reorganizeBlocks()`:
- Après un déplacement, collecte les positions finales de tous les blocs
- Notifie le listener via `onBlocksReordered()`

### 2. `PlanningFragment.java`

#### Mise à jour du listener:
Le listener implémente maintenant les deux méthodes:

```java
binding.dayTimeline.setMoveListener(new DayTimelineView.BlockMoveListener() {
    @Override
    public void onBlockMoved(long scheduledId, int newStart) {
        // Déplace un bloc dans la base de données
        repository.moveScheduled(...);
    }

    @Override
    public void onBlocksReordered(List<BlockReorderInfo> reordered) {
        // Met à jour TOUS les blocs réorganisés
        for (BlockReorderInfo info : reordered) {
            repository.moveScheduled(...);
        }
    }
});
```

## Comportement

### Lors du drag:
1. La carte se lève (élévation = 12f)
2. Les autres cartes qui chevauchent se déplacent automatiquement:
   - Si l'autre carte est au-dessus → elle se déplace plus haut (ou en bas si pas de place)
   - Si l'autre carte est en dessous → elle se déplace plus bas
3. Les positions sont mises à jour visuellement en temps réel

### À la fin du drag:
1. La carte retrouve son élévation normale (4f)
2. Tous les blocs sont réorganisés dans la base de données
3. En cas de chevauchement détecté par la repo → message d'erreur et rollback

## Avantages
✅ Drag & drop intuitif sur chaque carte
✅ Réorganisation automatique des autres tâches
✅ Feedback visuel en temps réel
✅ Respecte les contraintes de temps (6h–22h)
✅ Validation en base de données (pas de chevauchements)

## Notes techniques
- Le snapping à la grille est toujours appliqué (via `AgendaRepository.snapToGrid()`)
- La durée des tâches ne change pas, seuls les heures de début
- Les collisions sont détectées en pixels (pxPerMinute) puis converties en minutes
