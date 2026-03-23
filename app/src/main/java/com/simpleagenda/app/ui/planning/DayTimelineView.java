package com.simpleagenda.app.ui.planning;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.simpleagenda.app.R;
import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.ScheduledTaskWithTask;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.ui.common.TaskPalette;
import com.simpleagenda.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grille horaire (6h–22h) avec blocs déplaçables verticalement (tout le bloc est draggable).
 */
public class DayTimelineView extends FrameLayout {

    public interface BlockMoveListener {
        void onBlockMoved(long scheduledId, int newStartMinutesFromMidnight);
        void onBlocksReordered(List<BlockReorderInfo> reordered);
    }

    public static class BlockReorderInfo {
        public long scheduledId;
        public int newStartMinutesFromMidnight;

        public BlockReorderInfo(long scheduledId, int newStartMinutesFromMidnight) {
            this.scheduledId = scheduledId;
            this.newStartMinutesFromMidnight = newStartMinutesFromMidnight;
        }
    }

    /** Glisser-déposer d’une tâche sélectionnée depuis la liste vers la grille. */
    public interface ExternalDropListener {
        void onTaskDroppedFromPalette(long taskId, int rawStartMinutesFromMidnight);
    }

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float pxPerMinute;
    private int labelGutterPx;
    private BlockMoveListener moveListener;
    private ExternalDropListener externalDropListener;

    private final int dayStartMin = AgendaRepository.DAY_START_MINUTES;
    private final int dayEndMin = AgendaRepository.DAY_END_MINUTES;

    // Pour tracker les blocs et leurs positions
    private final Map<Long, BlockInfo> blockMap = new HashMap<>();

    private final int touchSlop;

    public DayTimelineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        float density = getResources().getDisplayMetrics().density;
        float hourPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, getResources().getDisplayMetrics());
        pxPerMinute = hourPx / 60f;
        labelGutterPx = (int) (40f * density);

        gridPaint.setColor(ContextCompat.getColor(context, R.color.divider));
        gridPaint.setStrokeWidth(1f);

        labelPaint.setColor(ContextCompat.getColor(context, R.color.on_surface_muted));
        labelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, getResources().getDisplayMetrics()));

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription() != null
                            && event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DROP:
                    return handlePaletteDrop(event.getClipData(), event.getY());
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    return true;
            }
        });
    }

    public void setMoveListener(@Nullable BlockMoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public void setExternalDropListener(@Nullable ExternalDropListener externalDropListener) {
        this.externalDropListener = externalDropListener;
    }

    private boolean handlePaletteDrop(@Nullable ClipData clip, float yInTimeline) {
        if (externalDropListener == null || clip == null || clip.getItemCount() == 0) {
            return false;
        }
        CharSequence text = clip.getItemAt(0).getText();
        if (text == null) {
            return false;
        }
        long taskId;
        try {
            taskId = Long.parseLong(text.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        int totalMin = dayEndMin - dayStartMin;
        float totalH = totalMin * pxPerMinute;
        yInTimeline = Math.max(0, Math.min(yInTimeline, totalH - 1));
        int rawStart = dayStartMin + Math.round(yInTimeline / pxPerMinute);
        externalDropListener.onTaskDroppedFromPalette(taskId, rawStart);
        return true;
    }

    public void setBlocks(@NonNull List<ScheduledTaskWithTask> blocks) {
        removeAllViews();
        blockMap.clear();
        int totalMin = dayEndMin - dayStartMin;
        int totalHeight = Math.round(totalMin * pxPerMinute);
        setMinimumHeight(totalHeight);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (ScheduledTaskWithTask st : blocks) {
            Task task = st.getTask();
            if (task == null) {
                continue;
            }
            MaterialCardView card = (MaterialCardView) inflater.inflate(R.layout.item_timeline_block, this, false);
            TextView time = card.findViewById(R.id.block_time);
            TextView title = card.findViewById(R.id.block_title);

            int start = st.getScheduled().getStartMinutesFromMidnight();
            int end = start + task.durationMinutes();
            time.setText(getContext().getString(
                    R.string.time_range_format,
                    TimeUtils.formatTime(start),
                    TimeUtils.formatTime(end)
            ));
            title.setText(task.getTitle());
            int light = TaskPalette.lightBackground(getContext(), task.getColorIndex());
            card.setCardBackgroundColor(light);
            card.setStrokeWidth(0);

            int durationMin = task.durationMinutes();
            int top = Math.round((start - dayStartMin) * pxPerMinute);
            int height = Math.round(durationMin * pxPerMinute);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    height
            );
            lp.leftMargin = labelGutterPx + (int) (4 * getResources().getDisplayMetrics().density);
            lp.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
            lp.topMargin = top;

            long scheduledId = st.getScheduled().getId();
            
            // Enregistrer les infos du bloc
            blockMap.put(scheduledId, new BlockInfo(card, scheduledId, durationMin, st.getScheduled().getStartMinutesFromMidnight()));

            card.setClickable(true);
            card.setOnTouchListener(new BlockDragHelper(scheduledId));

            card.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return event.getClipDescription() != null
                                && event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    case DragEvent.ACTION_DROP:
                        FrameLayout.LayoutParams lpCard = (FrameLayout.LayoutParams) v.getLayoutParams();
                        return handlePaletteDrop(event.getClipData(), lpCard.topMargin + event.getY());
                    default:
                        return false;
                }
            });

            addView(card, lp);
        }
    }
    
    private static class BlockInfo {
        MaterialCardView card;
        long scheduledId;
        int durationMinutes;
        int currentStart;
        
        BlockInfo(MaterialCardView card, long scheduledId, int durationMinutes, int originalStart) {
            this.card = card;
            this.scheduledId = scheduledId;
            this.durationMinutes = durationMinutes;
            this.currentStart = originalStart;
        }
    }

    /**
     * Remonte depuis le bloc jusqu’au {@link NestedScrollView} pour que le scroll parent
     * ne capture pas le geste pendant le déplacement.
     */
    private static void requestAncestorsDisallowInterceptFrom(View child, boolean disallow) {
        ViewParent p = child.getParent();
        while (p != null) {
            p.requestDisallowInterceptTouchEvent(disallow);
            if (p instanceof NestedScrollView) {
                break;
            }
            p = p.getParent();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int totalMin = dayEndMin - dayStartMin;
        float totalHeight = totalMin * pxPerMinute;
        for (int h = 6; h <= 22; h++) {
            int minuteOfHour = h * 60;
            float y = (minuteOfHour - dayStartMin) * pxPerMinute;
            canvas.drawLine(labelGutterPx, y, getWidth(), y, gridPaint);
            canvas.drawText(h + "h", 8f, y + labelPaint.getTextSize(), labelPaint);
        }
        canvas.drawLine(labelGutterPx, 0, labelGutterPx, totalHeight, gridPaint);
    }

    private int clampTop(int top, int height) {
        int totalMinSpan = dayEndMin - dayStartMin;
        int maxTop = Math.round(totalMinSpan * pxPerMinute) - height;
        return Math.max(0, Math.min(top, maxTop));
    }

    private int startFromTop(int top) {
        return dayStartMin + Math.round(top / pxPerMinute);
    }

    private int topFromStart(int startMinutes) {
        return Math.round((startMinutes - dayStartMin) * pxPerMinute);
    }

    private int snapToGrid(int minutes) {
        int relative = minutes - dayStartMin;
        int snapped = Math.round(relative / (float) MINUTES_BLOCK_SIZE) * MINUTES_BLOCK_SIZE;
        return dayStartMin + snapped;
    }

    private int ceilToGrid(int minutes) {
        int relative = minutes - dayStartMin;
        int snapped = ((relative + MINUTES_BLOCK_SIZE - 1) / MINUTES_BLOCK_SIZE) * MINUTES_BLOCK_SIZE;
        return dayStartMin + snapped;
    }

    private int floorToGrid(int minutes) {
        int relative = minutes - dayStartMin;
        int snapped = Math.max(0, (relative / MINUTES_BLOCK_SIZE) * MINUTES_BLOCK_SIZE);
        return dayStartMin + snapped;
    }

    private int clampStart(int startMinutes, int durationMinutes) {
        int maxStart = dayEndMin - durationMinutes;
        return Math.max(dayStartMin, Math.min(startMinutes, maxStart));
    }

    private List<BlockInfo> getSortedBlocks() {
        List<BlockInfo> blocks = new ArrayList<>(blockMap.values());
        Collections.sort(blocks, Comparator.comparingInt(info -> info.currentStart));
        return blocks;
    }

    private void animateBlockTop(@NonNull MaterialCardView card, int topMargin) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) card.getLayoutParams();
        if (lp.topMargin == topMargin) {
            return;
        }
        card.animate().cancel();
        card.animate()
                .y(topMargin)
                .setDuration(REORDER_ANIM_DURATION_MS)
                .withEndAction(() -> {
                    FrameLayout.LayoutParams updatedLp = (FrameLayout.LayoutParams) card.getLayoutParams();
                    updatedLp.topMargin = topMargin;
                    card.setLayoutParams(updatedLp);
                    card.setY(topMargin);
                })
                .start();
    }

    private final class BlockDragHelper implements View.OnTouchListener {
        private final long scheduledId;
        private float downRawY;
        private int startTopMargin;
        private boolean dragging;

        BlockDragHelper(long scheduledId) {
            this.scheduledId = scheduledId;
        }

        @Override
        @SuppressWarnings("ClickableViewAccessibility")
        public boolean onTouch(View v, MotionEvent event) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
            int totalMinSpan = dayEndMin - dayStartMin;
            int maxTop = Math.round(totalMinSpan * pxPerMinute) - lp.height;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    activeCard = card;
                    downRawY = event.getRawY();
                    startTopMargin = lp.topMargin;
                    dragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float dy = event.getRawY() - downRawY;
                    if (!dragging) {
                        if (Math.abs(dy) <= touchSlop) {
                            return true;
                        }
                        dragging = true;
                        requestAncestorsDisallowInterceptFrom(v, true);
                        v.bringToFront();
                        v.setElevation(16f);
                    }
                    int next = Math.round(startTopMargin + dy);
                    if (next < 0) {
                        next = 0;
                    }
                    if (next > maxTop) {
                        next = maxTop;
                    }
                    lp.topMargin = next;
                    v.setLayoutParams(lp);
                    return true;
                }

                case MotionEvent.ACTION_UP:
                    if (isDragging) {
                        finishDrag(info);
                        return true;
                    }
                    if (longPressRunnable != null) {
                        longPressHandler.removeCallbacks(longPressRunnable);
                        longPressRunnable = null;
                    }
                case MotionEvent.ACTION_CANCEL:
                    requestAncestorsDisallowInterceptFrom(v, false);
                    v.setElevation(4f);
                    if (dragging && moveListener != null) {
                        int newStart = dayStartMin + Math.round(lp.topMargin / pxPerMinute);
                        moveListener.onBlockMoved(scheduledId, newStart);
                    }
                    
                    // Après le déplacement, réorganiser les blocs
                    reorganizeBlocks();
                    
                    return true;

                default:
                    return false;
            }
        }

        private void beginDrag(@NonNull BlockInfo info) {
            previewOrder.clear();
            originalStarts.clear();
            previewSlotStarts.clear();
            List<BlockInfo> sorted = getSortedBlocks();
            for (BlockInfo block : sorted) {
                previewOrder.add(block.scheduledId);
                originalStarts.put(block.scheduledId, block.currentStart);
                previewSlotStarts.put(block.scheduledId, block.currentStart);
            }
            targetIndex = previewOrder.indexOf(scheduledId);
            lastPreviewDraggedStart = dragStartMinutes;
            lastUpdateMinutesBlock = dragStartMinutes / MINUTES_BLOCK_SIZE;
            activeCard.bringToFront();
            activeCard.setPressed(true);
            activeCard.animate().cancel();
            activeCard.animate().scaleX(1.02f).scaleY(1.02f).setDuration(90L).start();
            activeCard.setElevation(18f);
            updatePlacementOverlay(previewSlotStarts.get(scheduledId), info.durationMinutes);
            requestDisallowInterceptTouchEvent(true);
        }

        private void updateDragPosition(float rawY, @NonNull BlockInfo info) {
            int nextTop = clampTop(
                    Math.round(rawY - parentLocationOnScreen[1] - initialTouchOffset),
                    activeCard.getHeight()
            );

            activeCard.setY(nextTop);

            int currentMinutes = startFromTop(nextTop);
            int currentMinutesBlock = currentMinutes / MINUTES_BLOCK_SIZE;
            if (currentMinutesBlock != lastUpdateMinutesBlock) {
                lastUpdateMinutesBlock = currentMinutesBlock;
            }

            int proposedStart = clampStart(snapToGrid(currentMinutes), info.durationMinutes);
            if (proposedStart == lastPreviewDraggedStart) {
                return;
            }
            lastPreviewDraggedStart = proposedStart;
            moveDraggedToward(proposedStart, info);
            previewSlotStarts.put(scheduledId, proposedStart);
            info.currentStart = proposedStart;
            normalizePreviewLayout(info);

            Integer targetStart = previewSlotStarts.get(scheduledId);
            if (targetStart != null) {
                updatePlacementOverlay(targetStart, info.durationMinutes);
            }
        }

        private void moveDraggedToward(int proposedStart, @NonNull BlockInfo draggedInfo) {
            int guard = Math.max(1, previewOrder.size());
            boolean swapped;
            do {
                swapped = false;
                guard--;
                Integer currentStart = previewSlotStarts.get(scheduledId);
                if (currentStart == null) {
                    return;
                }

                if (proposedStart < currentStart && targetIndex > 0) {
                    long previousId = previewOrder.get(targetIndex - 1);
                    int previousEnd = getBlockEnd(previousId);
                    if (proposedStart < previousEnd) {
                        swapWithNeighbor(targetIndex - 1, draggedInfo);
                        swapped = true;
                        continue;
                    }
                }

                if (proposedStart > currentStart && targetIndex < previewOrder.size() - 1) {
                    long nextId = previewOrder.get(targetIndex + 1);
                    int proposedEnd = proposedStart + draggedInfo.durationMinutes;
                    if (proposedEnd > getBlockStart(nextId)) {
                        swapWithNeighbor(targetIndex + 1, draggedInfo);
                        swapped = true;
                    }
                }
            } while (swapped && guard > 0);
        }

        private int getBlockStart(long blockId) {
            Integer start = previewSlotStarts.get(blockId);
            return start != null ? start : dayEndMin;
        }

        private int getBlockEnd(long blockId) {
            BlockInfo info = blockMap.get(blockId);
            Integer start = previewSlotStarts.get(blockId);
            if (info == null || start == null) {
                return dayStartMin;
            }
            return start + info.durationMinutes;
        }

        private void swapWithNeighbor(int neighborIndex, @NonNull BlockInfo draggedInfo) {
            if (neighborIndex < 0 || neighborIndex >= previewOrder.size()) {
                return;
            }

            long neighborId = previewOrder.get(neighborIndex);
            BlockInfo neighbor = blockMap.get(neighborId);
            Integer draggedSlotStart = previewSlotStarts.get(scheduledId);
            Integer neighborSlotStart = previewSlotStarts.get(neighborId);
            if (neighbor == null || draggedSlotStart == null || neighborSlotStart == null) {
                return;
            }

            previewSlotStarts.put(scheduledId, neighborSlotStart);
            previewSlotStarts.put(neighborId, draggedSlotStart);
            Collections.swap(previewOrder, targetIndex, neighborIndex);
            targetIndex = neighborIndex;

            neighbor.currentStart = draggedSlotStart;
            animateBlockTop(neighbor.card, topFromStart(draggedSlotStart));

            draggedInfo.currentStart = neighborSlotStart;
            updatePlacementOverlay(neighborSlotStart, draggedInfo.durationMinutes);
        }

        private void normalizePreviewLayout(@NonNull BlockInfo draggedInfo) {
            Map<Long, Integer> normalized = new HashMap<>(previewSlotStarts);

            int previousEnd = dayStartMin;
            for (Long id : previewOrder) {
                BlockInfo block = blockMap.get(id);
                Integer preferredStart = normalized.get(id);
                if (block == null || preferredStart == null) {
                    continue;
                }
                int minStart = ceilToGrid(previousEnd);
                int clampedPreferred = clampStart(preferredStart, block.durationMinutes);
                int adjustedStart = Math.max(clampedPreferred, minStart);
                normalized.put(id, adjustedStart);
                previousEnd = adjustedStart + block.durationMinutes;
            }

            int nextStart = dayEndMin;
            for (int i = previewOrder.size() - 1; i >= 0; i--) {
                long id = previewOrder.get(i);
                BlockInfo block = blockMap.get(id);
                Integer currentStart = normalized.get(id);
                if (block == null || currentStart == null) {
                    continue;
                }
                int maxStart = floorToGrid(nextStart - block.durationMinutes);
                int adjustedStart = Math.min(currentStart, maxStart);
                adjustedStart = clampStart(adjustedStart, block.durationMinutes);
                normalized.put(id, adjustedStart);
                nextStart = adjustedStart;
            }

            previousEnd = dayStartMin;
            for (Long id : previewOrder) {
                BlockInfo block = blockMap.get(id);
                Integer currentStart = normalized.get(id);
                if (block == null || currentStart == null) {
                    continue;
                }
                int minStart = ceilToGrid(previousEnd);
                int adjustedStart = Math.max(currentStart, minStart);
                adjustedStart = clampStart(adjustedStart, block.durationMinutes);
                normalized.put(id, adjustedStart);
                previousEnd = adjustedStart + block.durationMinutes;
            }

            previewSlotStarts.clear();
            previewSlotStarts.putAll(normalized);

            Integer draggedStart = previewSlotStarts.get(scheduledId);
            if (draggedStart != null) {
                draggedInfo.currentStart = draggedStart;
            }

            for (Long id : previewOrder) {
                if (id == scheduledId) {
                    continue;
                }
                BlockInfo block = blockMap.get(id);
                Integer start = previewSlotStarts.get(id);
                if (block == null || start == null) {
                    continue;
                }
                block.currentStart = start;
                animateBlockTop(block.card, topFromStart(start));
            }
        }

        private void finishDrag(@NonNull BlockInfo draggedInfo) {
            isDragging = false;
            requestDisallowInterceptTouchEvent(false);
            placementOverlay.setVisibility(View.GONE);
            activeCard.setPressed(false);
            activeCard.animate().cancel();
            activeCard.animate().scaleX(1f).scaleY(1f).setDuration(90L).start();
            activeCard.setElevation(4f);

            List<BlockReorderInfo> changedBlocks = new ArrayList<>();
            for (Long id : previewOrder) {
                BlockInfo block = blockMap.get(id);
                Integer targetStart = previewSlotStarts.get(id);
                Integer previousStart = originalStarts.get(id);
                if (block == null || targetStart == null) {
                    continue;
                }
                block.currentStart = targetStart;
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) block.card.getLayoutParams();
                lp.topMargin = topFromStart(targetStart);
                block.card.setLayoutParams(lp);
                block.card.setY(lp.topMargin);
                if (previousStart != null && previousStart != targetStart) {
                    changedBlocks.add(new BlockReorderInfo(id, targetStart));
                }
            }

            if (moveListener != null) {
                if (!changedBlocks.isEmpty()) {
                    moveListener.onBlocksReordered(changedBlocks);
                } else {
                    moveListener.onBlockMoved(scheduledId, draggedInfo.currentStart);
                }
            }

            activeCard = null;
            lastPreviewDraggedStart = Integer.MIN_VALUE;
        }

        private void cancelDrag(@NonNull BlockInfo draggedInfo) {
            isDragging = false;
            requestDisallowInterceptTouchEvent(false);
            placementOverlay.setVisibility(View.GONE);
            activeCard.setPressed(false);
            activeCard.animate().cancel();
            activeCard.animate().scaleX(1f).scaleY(1f).setDuration(90L).start();
            activeCard.setElevation(4f);

            for (Long id : previewOrder) {
                BlockInfo block = blockMap.get(id);
                Integer start = originalStarts.get(id);
                if (block == null || start == null) {
                    continue;
                }
                int top = topFromStart(start);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) block.card.getLayoutParams();
                lp.topMargin = top;
                block.card.setLayoutParams(lp);
                block.card.setY(top);
                block.currentStart = start;
            }

            draggedInfo.currentStart = dragStartMinutes;
            activeCard = null;
            lastPreviewDraggedStart = Integer.MIN_VALUE;
        }
    }
}
