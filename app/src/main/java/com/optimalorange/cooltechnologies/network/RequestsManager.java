package com.optimalorange.cooltechnologies.network;

import com.android.volley.Request;

/**
 * {@link Request Requests}管理器。用于统计Requests状态。
 */
public class RequestsManager {

    private final VolleySingleton mVolleySingleton;

    private int mRequests = 0;

    private int mRequestRespondeds = 0;

    private int mRequestErrors = 0;

    private int mRequestCancelleds = 0;

    private OnAllRequestsFinishedListener mOnAllRequestsFinishedListener;

    public RequestsManager(VolleySingleton volleySingleton) {
        mVolleySingleton = volleySingleton;
    }

    /**
     * 取消所有{@link #getRequestPending() PendingRequest}，
     * 并重置{@link #getRequests() 发出请求数}数为0。
     * 取消后重置请求数前，触发{@link OnAllRequestsFinishedListener}事件。
     */
    public void reset() {
        cancelAllRequest();
        resetRequests();
    }

    /**
     * 取消所有{@link #getRequestPending() PendingRequest}，
     * 并重置{@link #getRequests() 发出请求数}数为0。不触发{@link OnAllRequestsFinishedListener}事件。
     */
    public void resetSilently() {
        cancelAllRequestSilently();
        resetRequests();
    }

    private void resetRequests() {
        mRequests = mRequestRespondeds = mRequestErrors = mRequestCancelleds = 0;
    }

    /**
     * 添加{@link Request}数
     *
     * @return 添加后，总Request数
     */
    public int addRequest(Request request) {
        request.setTag(this);
        mVolleySingleton.addToRequestQueue(request);
        return mRequests++;
    }

    /**
     * 添加收到响应的{@link Request}数
     *
     * @return 添加后，总收到响应的Request数
     */
    public int addRequestRespondeds() {
        int result = mRequestRespondeds++;
        checkIsAllRequestsFinished();
        return result;
    }

    /**
     * 添加失败的{@link Request}数
     *
     * @return 添加后，总失败的Request数
     */
    public int addRequestErrors() {
        int result = mRequestErrors++;
        checkIsAllRequestsFinished();
        return result;
    }

    /**
     * 添加取消的{@link Request}数
     *
     * @return 添加后，总取消的Request数
     */
    public int addRequestCancelleds() {
        int result = mRequestCancelleds++;
        checkIsAllRequestsFinished();
        return result;
    }

    /**
     * 取消所有{@link #getRequestPending() PendingRequest}。
     * 触发{@link OnAllRequestsFinishedListener}事件。
     *
     * @see #cancelAllRequestSilently()
     */
    public void cancelAllRequest() {
        cancelAllRequestSilently();
        checkIsAllRequestsFinished();
    }

    /**
     * 取消所有{@link #getRequestPending() PendingRequest}。
     * 不触发{@link OnAllRequestsFinishedListener}事件。
     *
     * @see #cancelAllRequest()
     */
    public void cancelAllRequestSilently() {
        mVolleySingleton.getRequestQueue().cancelAll(this);
        mRequestCancelleds = mRequests - getRequestFinisheds();
    }

    public void setOnAllRequestsFinishedListener(
            OnAllRequestsFinishedListener onAllRequestsFinishedListener) {
        mOnAllRequestsFinishedListener = onAllRequestsFinishedListener;
    }

    public VolleySingleton getVolleySingleton() {
        return mVolleySingleton;
    }

    /**
     * 取得此前发出的请求的数目
     */
    public int getRequests() {
        return mRequests;
    }

    public int getRequestRespondeds() {
        return mRequestRespondeds;
    }

    public int getRequestErrors() {
        return mRequestErrors;
    }

    public int getRequestCancelleds() {
        return mRequestCancelleds;
    }

    public int getRequestFinisheds() {
        return mRequestRespondeds + mRequestErrors;
    }

    /**
     * 取得等待完成的（尚未完成，也没取消的）请求数。
     */
    public int getRequestPending() {
        return mRequests - getRequestFinisheds() - mRequestCancelleds;
    }

    public boolean isAllRequestsFinished() {
        return getRequestPending() == 0;
    }

    private void checkIsAllRequestsFinished() {
        if (isAllRequestsFinished() && mOnAllRequestsFinishedListener != null) {
            mOnAllRequestsFinishedListener.onAllRequestsFinished(this);
        }
    }


    public interface OnAllRequestsFinishedListener {

        /**
         * 当所有{@link Request Requests}都完成时，被调用
         *
         * @param requestsManager 事件源。可以用于查询请求完成情况。
         */
        void onAllRequestsFinished(RequestsManager requestsManager);
    }

}
