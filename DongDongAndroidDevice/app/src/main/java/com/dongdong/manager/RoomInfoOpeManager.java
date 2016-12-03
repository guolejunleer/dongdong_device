package com.dongdong.manager;

import android.content.Context;

import com.dongdong.DeviceApplication;
import com.dongdong.bean.RoomInfoBean;
import com.dongdong.db.RoomCardOpe;
import com.dongdong.db.RoomIndexOpe;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.db.entry.RoomIndexBean;
import com.dongdong.utils.DDLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomInfoOpeManager {

    public static void checkRoomInfo(Context context, CopyOnWriteArrayList<RoomInfoBean>
            roomInfoSet, int maxCount) {
        List<RoomInfoBean> tempRoomInfoList = new ArrayList<>();
        int count = 0;
        for (RoomInfoBean bean : roomInfoSet) {
            if (count > maxCount) {
                break;
            }
            count++;
            tempRoomInfoList.add(bean);
            int roomId = bean.getRoomId();
            int cardIndex = bean.getCardIndex();

            RoomIndexBean localRCBean = RoomIndexOpe.queryDataByRoomId(context, roomId);
            DDLog.i("RoomInfoOpeManager.clazz--->>>roomId:" + roomId
                    + ",cardIndex:" + cardIndex + ",localRCBean:" + localRCBean);
            if (cardIndex == 0) {//A.cardIndex为0表明平台将整个房号都删除,本地删除返回
                if (localRCBean != null) {
                    RoomIndexOpe.deleteDataByCardRoomId(context, roomId);
                }
                List<RoomCardBean> rcBeanList = RoomCardOpe.queryDataListByRoomId(context, roomId);
                if (rcBeanList != null && !rcBeanList.isEmpty()) {
                    RoomCardOpe.deleteDataByRoomId(context, roomId);
                }
                continue;
            } else {//B.cardIndex不为0，先查询本地有没有这个roomId的信息，有则更新，没有则添加
                if (localRCBean != null) {
                    localRCBean.setCardIndex(cardIndex);
                    RoomIndexOpe.updateRoomIndexByBean(context, localRCBean);
                } else {
                    RoomIndexBean roomIndexBean = new RoomIndexBean();
                    roomIndexBean.setRoomId(roomId);
                    roomIndexBean.setCardIndex(cardIndex);
                    RoomIndexOpe.insertData(context, roomIndexBean);
                }
            }
            //卡号处理
            List<RoomCardBean> platCards = bean.getPlatCards();
            List<RoomCardBean> localRCBeans = RoomCardOpe.queryDataListByRoomId(context, roomId);
            if (platCards.isEmpty()) {
                //C.platCards为空说明这个房号下没卡，本地删除这个房号下所有的卡
                if (localRCBeans != null && !localRCBeans.isEmpty()) {
                    RoomCardOpe.deleteDataByRoomId(context, roomId);
                }
            } else {
                if (localRCBeans != null && !localRCBeans.isEmpty()) {
                    for (RoomCardBean rcBean : localRCBeans) {
                        boolean isExist = false;
                        for (RoomCardBean platCard : platCards) {
                            if (rcBean.getCardNum().equals(platCard.getCardNum())) {
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist) {
                            //D.平台卡号与本地卡号对比，如果本地卡号没有的卡说明为旧卡，要删除
                            RoomCardOpe.deleteDataByRoomCardBeanId(context, rcBean.getId());
                        }
                    }
                    for (RoomCardBean platCard : platCards) {
                        boolean isExist = false;
                        for (RoomCardBean rcBean : localRCBeans) {
                            if (rcBean.getCardNum().equals(platCard.getCardNum())) {
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist) {
                            //E.本地卡号与平台卡号对比，如果本地没有的卡说明为新卡，要新增
                            RoomCardOpe.insertData(context, platCard);
                        }
                    }
                } else {
                    //F.本地全部都没有数据，添加平台的卡号数据
                    RoomCardOpe.insertData(context, platCards);
                }
            }
        }
        //校验插入数据是否成功
        verifyRoomCard(tempRoomInfoList, context);
    }

    private static void verifyRoomCard(List<RoomInfoBean> roomInfoList, Context context) {
        for (RoomInfoBean bean : roomInfoList) {
            int roomId = bean.getRoomId();
            int cardIndex = bean.getCardIndex();
            DDLog.i("RoomInfoOpeManager.clazz--->>>verifyRoomCard roomId:" + roomId
                    + ",cardIndex:" + cardIndex);
            if (cardIndex == 0) {//A.如果cardIndex为0，去数据库查询，如果还有数据，说明没有删除成功
                RoomIndexBean localRCBean = RoomIndexOpe.queryDataByRoomId(context, roomId);
                if (localRCBean == null) {
                    boolean remove = DeviceApplication.mVerifyRoomList.remove(bean);
                    DDLog.i("RoomInfoOpeManager.clazz--->>>verifyRoomCard cardIndex=0 remove:" + remove);
                }
            } else {//B.如果cardIndex不为0，那么去数据库查询卡号，如果还有数据不同，说明没有删除成功
                List<RoomCardBean> platCards = bean.getPlatCards();
                List<RoomCardBean> localRCBeans = RoomCardOpe.queryDataListByRoomId(
                        context, roomId);
                if (platCards.size() == localRCBeans.size()) {
                    boolean containsAll = platCards.containsAll(localRCBeans);
                    if (containsAll) {
                        boolean remove = DeviceApplication.mVerifyRoomList.remove(bean);
                        DDLog.i("RoomInfoOpeManager.clazz--->>>verifyRoomCard containsAll remove:" + remove);
                    }
                }
            }
        }
    }
}
