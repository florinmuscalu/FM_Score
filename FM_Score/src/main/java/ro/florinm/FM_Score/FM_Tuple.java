package ro.florinm.FM_Score;

import android.graphics.Canvas;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

public class FM_Tuple {
    int index;
    List<FM_Note> n;
    int size;
    FM_Score score;

    public FM_Tuple(FM_Score score, int size, int index) {
        n = new ArrayList<>();
        this.size = size;
        this.index = index;
        this.score = score;
    }

    public void AddNote(FM_Note n) {
        this.n.add(n);
    }

    public void Draw(Canvas canvas) {
        float x, xe, y, ye;
        float StaveLineHalfWidth = FM_Const.dpTOpx(score.getContext(), 0.25f);
        if (n.get(0).stem_up) {
            x = 0.5f * score.getDistanceBetweenStaveLines() + n.get(0).startX + n.get(0).paddingLeft + n.get(0).WidthAccidental() + n.get(0).paddingNote;
            xe = 0.5f * score.getDistanceBetweenStaveLines() + n.get(n.size() - 1).startX + n.get(n.size() - 1).paddingLeft + n.get(n.size() - 1).WidthAccidental() + n.get(n.size() - 1).paddingNote + n.get(n.size() - 1).WidthNote();
            y = n.get(0).ys + (n.get(0).getDisplacement() + 0.5f) * score.getDistanceBetweenStaveLines() - n.get(0).Height(true) + score.getDistanceBetweenStaveLines() / 2;
            ye = n.get(n.size() - 1).ys + (n.get(n.size() - 1).getDisplacement() + 0.5f) * score.getDistanceBetweenStaveLines() - n.get(n.size() - 1).Height(true) + score.getDistanceBetweenStaveLines() / 2;

            float yMiddleMin = n.get(1).ys + (n.get(1).getDisplacement() + 0.5f) * score.getDistanceBetweenStaveLines() - n.get(1).Height(true) + score.getDistanceBetweenStaveLines() / 2;

            for (int i = 2; i < n.size() - 1; i++) {
                float yMiddle = n.get(i).ys + (n.get(i).getDisplacement() + 0.5f) * score.getDistanceBetweenStaveLines() - n.get(i).Height(true) + score.getDistanceBetweenStaveLines() / 2;
                if (yMiddle < yMiddleMin) yMiddleMin = yMiddle;
            }
            if ((y + ye) / 2 > yMiddleMin) {
                float diff = (y + ye) / 2 - yMiddleMin;
                y = y - diff;
                ye = ye - diff;
            }
        } else {
            x = n.get(0).startX +
                    n.get(0).paddingLeft +
                    n.get(0).WidthAccidental() +
                    n.get(0).paddingNote -
                    0.5f * score.getDistanceBetweenStaveLines() ;
            xe = n.get(n.size() - 1).startX +
                    n.get(n.size() - 1).paddingLeft +
                    n.get(n.size() - 1).WidthAccidental() +
                    n.get(n.size() - 1).WidthNote() +
                    n.get(n.size() - 1).paddingNote -
                    0.5f * score.getDistanceBetweenStaveLines();

            y = n.get(0).ys +
                    (n.get(0).getDisplacement() - 0.5f) * score.getDistanceBetweenStaveLines() +
                    n.get(0).Height(true) -
                    score.getDistanceBetweenStaveLines() / 2;
            ye = n.get(n.size() - 1).ys +
                    (n.get(n.size() - 1).getDisplacement() - 0.5f) * score.getDistanceBetweenStaveLines() +
                    n.get(n.size() - 1).Height(true) -
                    score.getDistanceBetweenStaveLines() / 2;

            float yMiddleMin = n.get(1).ys + (n.get(1).getDisplacement()) * score.getDistanceBetweenStaveLines() + n.get(1).Height(true) - score.getDistanceBetweenStaveLines() / 2;

            for (int i = 2; i < n.size() - 1; i++) {
                float yMiddle = n.get(i).ys + (n.get(i).getDisplacement()) * score.getDistanceBetweenStaveLines() + n.get(i).Height(true) - score.getDistanceBetweenStaveLines() / 2;
                if (yMiddle > yMiddleMin) yMiddleMin = yMiddle;
            }
            if ((y + ye) / 2 < yMiddleMin) {
                float diff = (y + ye) / 2 - yMiddleMin;
                y = y - diff;
                ye = ye - diff;
            }
        }

        String text = FM_Const._3;
        if (size == 5) text = FM_Const._5;

        Path topPath;
        if (!n.get(0).beam) {
            if (n.get(0).stem_up) {
                canvas.drawRect(x - StaveLineHalfWidth, y, x + StaveLineHalfWidth, y - score.getDistanceBetweenStaveLines(), score.Font);
                canvas.drawRect(xe - StaveLineHalfWidth, ye, xe + StaveLineHalfWidth, ye - score.getDistanceBetweenStaveLines(), score.Font);
            } else {
                canvas.drawRect(x - StaveLineHalfWidth, y, x + StaveLineHalfWidth, y + score.getDistanceBetweenStaveLines(), score.Font);
                canvas.drawRect(xe - StaveLineHalfWidth, ye, xe + StaveLineHalfWidth, ye + score.getDistanceBetweenStaveLines(), score.Font);
            }
        }
        FM_Const.AdjustFont(score, text, 1);
        float w = score.Font.measureText(text);
        float middle1 = (x + xe) / 2 - w / 2 - score.getDistanceBetweenStaveLines() / 2;
        float middle2 = (x + xe) / 2 + w / 2 + score.getDistanceBetweenStaveLines() / 2;
        float slope = FM_Const.slope(x, y - score.getDistanceBetweenStaveLines(), xe, ye - score.getDistanceBetweenStaveLines());

        if (!n.get(0).beam) {
            if (n.get(0).stem_up) {
                topPath = new Path();
                topPath.reset();
                topPath.moveTo(x, y - score.getDistanceBetweenStaveLines() + 2 * StaveLineHalfWidth);
                topPath.lineTo(middle1, slope * (middle1 - x) + y - score.getDistanceBetweenStaveLines() + 2 * StaveLineHalfWidth);
                topPath.lineTo(middle1, slope * (middle1 - x) + y - score.getDistanceBetweenStaveLines());
                topPath.lineTo(x, y - score.getDistanceBetweenStaveLines());
                topPath.lineTo(x, y - score.getDistanceBetweenStaveLines() + 2 * StaveLineHalfWidth);
                canvas.drawPath(topPath, score.Font);

                topPath = new Path();
                topPath.reset();
                topPath.moveTo(middle2, ye - score.getDistanceBetweenStaveLines() - slope * (xe - middle2) + 2 * StaveLineHalfWidth);
                topPath.lineTo(xe, ye - score.getDistanceBetweenStaveLines() + 2 * StaveLineHalfWidth);
                topPath.lineTo(xe, ye - score.getDistanceBetweenStaveLines());
                topPath.lineTo(middle2, ye - score.getDistanceBetweenStaveLines() - slope * (xe - middle2));
                topPath.lineTo(middle2, ye - score.getDistanceBetweenStaveLines() - slope * (xe - middle2) + 2 * StaveLineHalfWidth);
                canvas.drawPath(topPath, score.Font);
            } else {
                topPath = new Path();
                topPath.reset();
                topPath.moveTo(x, y + score.getDistanceBetweenStaveLines() - 2 * StaveLineHalfWidth);
                topPath.lineTo(middle1, slope * (middle1 - x) + y + score.getDistanceBetweenStaveLines() - 2 * StaveLineHalfWidth);
                topPath.lineTo(middle1, slope * (middle1 - x) + y + score.getDistanceBetweenStaveLines());
                topPath.lineTo(x, y + score.getDistanceBetweenStaveLines());
                topPath.lineTo(x, y + score.getDistanceBetweenStaveLines() - 2 * StaveLineHalfWidth);
                canvas.drawPath(topPath, score.Font);
                topPath = new Path();
                topPath.reset();
                topPath.moveTo(middle2, ye + score.getDistanceBetweenStaveLines() - slope * (xe - middle2) - 2 * StaveLineHalfWidth);
                topPath.lineTo(xe, ye + score.getDistanceBetweenStaveLines() - 2 * StaveLineHalfWidth);
                topPath.lineTo(xe, ye + score.getDistanceBetweenStaveLines());
                topPath.lineTo(middle2, ye + score.getDistanceBetweenStaveLines() - slope * (xe - middle2));
                topPath.lineTo(middle2, ye + score.getDistanceBetweenStaveLines() - slope * (xe - middle2) - 2 * StaveLineHalfWidth);
                canvas.drawPath(topPath, score.Font);
            }
        }
        //canvas.drawLine(x, y - stave.getDistanceBetweenStaveLines(), xe,ye - stave.getDistanceBetweenStaveLines() , stave.StaveFont);
        if (n.get(0).stem_up) canvas.drawText(text, (x + xe) / 2 - w / 2, (y + ye) / 2 - score.getDistanceBetweenStaveLines(), score.Font);
        else canvas.drawText(text, (x + xe) / 2 - w / 2, (y + ye) / 2 + score.getDistanceBetweenStaveLines(), score.Font);
    }
}
