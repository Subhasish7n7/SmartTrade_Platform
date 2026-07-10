import { Navbar } from "@/components/marketplace/navbar";
import { ChatPageClient } from "@/components/marketplace/chat/chat-page-client";
import { requireUser } from "@/lib/auth/require-user";

export default async function MessagesPage() {
  const currentPath = "/messages"
  const user = await requireUser(currentPath)
  

  return (
    <div className="h-screen overflow-hidden bg-background">
      <Navbar />
      <main className="pt-16 h-[calc(100vh-1rem)]">
        <ChatPageClient currentUserId={user?.id} />
      </main>
    </div>
  );
}
