package com.shinefy.ptr;

import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Map;

import javax.annotation.Nullable;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;


/**
 * Created by shinefy on 2017/12/18.
 */

public class UltraPullToRefreshViewManager extends ViewGroupManager<PtrClassicFrameLayout> {

    private final static int COMMAND_REFRESH_COMPLETE = 0;
    private final static int COMMAND_AUTO_REFRESH = 1;


    @Override
    public String getName() {
        return "RCTUltraPullToRefresh";
    }

    @Override
    protected PtrClassicFrameLayout createViewInstance(ThemedReactContext reactContext) {
        PtrClassicFrameLayout frameLayout = new PtrClassicFrameLayout(reactContext);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        PtrMyTextHeader header = new PtrMyTextHeader(reactContext);
        frameLayout.setHeaderView(header);
        frameLayout.addPtrUIHandler(header);
        frameLayout.disableWhenHorizontalMove(true);
        return frameLayout;
    }

    ///////////////////////////////////////////////////////////////////////////
    // native向js发送事件
    ///////////////////////////////////////////////////////////////////////////

    //覆写该函数，将 onRefresh 这个事件名在 JavaScript 端映射到 onRefresh 回调属性上
    @Nullable
    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("onRefresh", MapBuilder.of("registrationName", "onRefresh"))
                .build();
    }

    //覆写addEventEmitters，onRefreshBegin回调时传递native事件传递给JS
    @Override
    protected void addEventEmitters(final ThemedReactContext reactContext, final PtrClassicFrameLayout view) {
        view.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new RefreshEvent(view.getId()));
            }
        });
    }

    //定义需要传递的native事件
    private static class RefreshEvent extends Event<RefreshEvent> {

        public RefreshEvent(int viewTag) {
            super(viewTag);
        }

        @Override
        public String getEventName() {
            return "onRefresh";
        }

        @Override
        public void dispatch(RCTEventEmitter rctEventEmitter) {
            rctEventEmitter.receiveEvent(getViewTag(), getEventName(), null);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // js向native发送命令
    ///////////////////////////////////////////////////////////////////////////

    //返回native想要接收的js指令的集合
    //JS调用UIManagerModule类中的dispatchViewManagerCommand方法，传入对应的指令ID 来调用 Java 层的事件。在 RN 中就是这样实现在 JS 层调用 Java 层的代码。
    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("refreshComplete", COMMAND_REFRESH_COMPLETE, "autoRefresh", COMMAND_AUTO_REFRESH);
    }

    //这个和getCommandsMap是对应的，处理接收到对应指令后的逻辑。
    @Override
    public void receiveCommand(PtrClassicFrameLayout root, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_REFRESH_COMPLETE:
                root.refreshComplete();
                break;
            case COMMAND_AUTO_REFRESH:
                root.autoRefresh();
                break;
            default:
                super.receiveCommand(root, commandId, args);
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // ptr源码是在onFinishInflate中findViewById并初始化contentView和headerView的
    // 所以rn中只能手动调用去初始化了
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void addView(PtrClassicFrameLayout parent, View child, int index) {
        super.addView(parent, child, 1);
        parent.onFinishInflateRN();
    }


    ///////////////////////////////////////////////////////////////////////////
    // 为rn导出属性
    ///////////////////////////////////////////////////////////////////////////

    //阻尼系数。越大，感觉下拉时越吃力
    @ReactProp(name = "resistance", defaultFloat = 1.7f)
    public void setResistances(PtrClassicFrameLayout view, float resistance) {
        view.setResistance(resistance);
    }

    //触发刷新时移动的位置比例。移动达到头部高度1.2倍时可触发刷新操作
    @ReactProp(name = "ratioOfHeaderHeightToRefresh", defaultFloat = 1.2f)
    public void setRatioOfHeaderHeightToRefresh(PtrClassicFrameLayout view, float ratioOfHeaderHeight) {
        view.setRatioOfHeaderHeightToRefresh(ratioOfHeaderHeight);
    }

    //下拉刷新 / 释放刷新
    @ReactProp(name = "pullToRefresh", defaultBoolean = false)
    public void setPullToRefresh(PtrClassicFrameLayout view, boolean pullToRefresh) {
        view.setPullToRefresh(pullToRefresh);
    }

    //刷新是否保持头部
    @ReactProp(name = "keepHeaderWhenRefresh", defaultBoolean = true)
    public void setKeepHeaderWhenRefresh(PtrClassicFrameLayout view, boolean keepHeaderWhenRefresh) {
        view.setKeepHeaderWhenRefresh(keepHeaderWhenRefresh);
    }



}
