package com.optimalorange.cooltechnologies.util;

public class ItemsCountCalculater {

    private static final double DISCRIMINANT = 0.00000000000000001236;

    private ItemsCountCalculater() {
        // 禁止实例化
    }

    /**
     * 计算能够在指定containerDimen内，盛放[min, max]范围内大小的Item View的个数，
     * 以及相应的Item View的尺寸。<br />
     * Note: 如果min或marginTotal很大（min + marginTotal > containerDimen），
     * {@link Result#getCount() result.getCount()}为0，
     * {@link Result#getDimension() result.getDimension()}为{@link Integer#MAX_VALUE}。
     *
     * @param min            最小尺寸
     * @param max            最大尺寸
     * @param marginTotal    在所计算维度上的，总margin尺寸。例如在计算width时，就是左右margin之和。
     * @param containerDimen item View所在的父View的尺寸
     * @param preference     计算偏好
     * @see android.util.TypedValue#applyDimension(int unit, float value,
     * android.util.DisplayMetrics metrics)
     * @see Preference
     */
    public static Result calculateItemsCountAndDimension(
            double min, double max, double marginTotal, double containerDimen,
            Preference preference) {
        if (min > max) {
            throw new IllegalArgumentException("min > max");
        }
        if (min < 0 || max <= 0 || marginTotal < 0) {
            throw new IllegalArgumentException("min < 0 || max <= 0 || marginTotal < 0");
        }
        if (Math.abs(min) < DISCRIMINANT && preference == Preference.SMALLER_OR_MORE_ITEMS) {
            throw new IllegalArgumentException("min==0 && preference is SMALLER_OR_MORE_ITEMS");
        }
        if (containerDimen <= 0) {
            throw new IllegalArgumentException("containerDimen <= 0");
        }
        //TODO 改善算法
        int minCount = (int) (containerDimen / (max + marginTotal));
        int maxCount = (int) (containerDimen / (min + marginTotal));
        // ↑这个表达式中 if(min==0) maxCount = Integer.MAX_VALUE
        int resultCount;
        switch (preference) {
            case SMALLER_OR_MORE_ITEMS:
                resultCount = maxCount;
                break;
            case BIGGER_OR_LESS_ITEMS:
                // assert minCount >= 0;
                if (minCount > 0) {
                    resultCount = minCount;
                } else { // minCount == 0
                    resultCount = maxCount > 0 ? 1 : 0; //1 is minCount + 1
                }
                break;
            default:
                throw new IllegalArgumentException("Please set a legal preference parameter.");
        }
        int resultDimension = (int) (containerDimen / resultCount - marginTotal + 0.5);
        // + 0.5 为了四舍五入     ↑这个表达式中，如果resultCount==0，结果为Integer.MAX_VALUE
        if (resultDimension > max && resultCount > 0) {
            resultDimension = (int) max;
        }
        return new Result(resultCount, resultDimension);
    }

    /**
     * 计算偏好
     *
     * @see Preference#SMALLER_OR_MORE_ITEMS
     * @see Preference#BIGGER_OR_LESS_ITEMS
     */
    public static enum Preference {
        /**
         * 尽量分配更多的item，或者说计算出的item尺寸尽量小
         */
        SMALLER_OR_MORE_ITEMS,
        /**
         * 尽量分配更少的item，或者说计算出的item尺寸尽量大
         */
        BIGGER_OR_LESS_ITEMS
    }

    public static class Result {

        private final int mCount;

        private final int mDimension;

        public Result(int count, int dimension) {
            mCount = count;
            mDimension = dimension;
        }

        /**
         * @return Item View应当的大小，单位：像素
         */
        public int getDimension() {
            return mDimension;
        }

        /**
         * @return Item应当的个数
         */
        public int getCount() {
            return mCount;
        }
    }

}
