import { ChatPageClient } from "@/components/chat/chat-page-client"
import { Navbar } from "@/components/marketplace/navbar"
import { requireUser } from "@/lib/auth/require-user"

interface Props {
  params: Promise<{
    selectedUserId: string
  }>
}

export default async function MessagesPage({ params }: Props) {
  const { selectedUserId } = await params

  const currentPath = `/messages/${selectedUserId}`
  const user = await requireUser(currentPath)

  const userId = Number(selectedUserId)

  if (!Number.isInteger(userId)) {
    // Replace with your preferred not-found handling.
    throw new Error("Invalid user ID")
  }

  return (
    <div className="h-screen overflow-hidden bg-background">
      <Navbar />

      <main className="pt-16 h-[calc(100vh-1rem)]">
        <ChatPageClient currentUserId={user.id} selectedUserId={userId} />
      </main>
    </div>
  )
}
