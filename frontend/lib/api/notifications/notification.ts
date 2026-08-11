import { notficationResponse } from "@/lib/types/api/notifications";
import { clientApi } from "../configs/client";

export async function getNotifications(){
    const response = await clientApi.get<notficationResponse>("/notifications");
    return response.data;
}

export async function deleteNotification(id:number){
    const response = await clientApi.get<notficationResponse>(`/notifications/${id}`);
    return response.data;
}

export async function markAllRead(){
    const response = await clientApi.get<notficationResponse>("/notifications/read-all");
    return response.data;
}

export async function markRead(id:number){
    const response = await clientApi.get<notficationResponse>(`/notifications/${id}/read`);
    return response.data;
}

export async function unreadCount(){
    const response = await clientApi.get<notficationResponse>("/notifications/unread-count");
    return response.data;
}