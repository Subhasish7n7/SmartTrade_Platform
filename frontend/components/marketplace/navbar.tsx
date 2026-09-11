"use client"

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import { logout } from "@/lib/api/auth/auth.client"
import { stompClient } from "@/lib/api/configs/websocket"
import { useAuth } from "@/lib/providers/auth-provider"
import { useNotifications } from "@/lib/providers/notification-provider"
import { cn } from "@/lib/utils"
import { AnimatePresence, motion } from "framer-motion"
import { ArrowLeftRight, Bell, ChevronDown, Heart, LogOut, Menu, MessageCircle, Package, Search, Settings, User, X } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import * as React from "react"
import { toast } from "sonner"
import { Notification } from "./notification"

interface NavbarProps {
  className?: string
}

export function Navbar({ className }: NavbarProps) {
  const [isMenuOpen, setIsMenuOpen] = React.useState(false)
  const [isSearchOpen, setIsSearchOpen] = React.useState(false)
  const [scrolled, setScrolled] = React.useState(false)
  const router = useRouter()
  const { user, setUser } = useAuth()

  const { unreadCount: notificationCount, tradeAlertCount, chatAlertCount, markModuleNotificationsRead } = useNotifications()

  React.useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 10)
    }
    window.addEventListener("scroll", handleScroll)
    return () => window.removeEventListener("scroll", handleScroll)
  }, [])

  const handleLogout = async () => {
    try {
      // Stop the authenticated WebSocket session first.
      await stompClient.deactivate()

      // Clear the HttpOnly JWT cookie on the backend.
      await logout()

      // Remove the authenticated user from application state.
      setUser(null)

      router.replace("/login")
      router.refresh()
    } catch (error) {
      console.error("Logout failed", error)

      toast.error("Failed to log out")
    }
  }
  const navItems = [
    { label: "Marketplace", href: "/" },
    { label: "My Inventory", href: "/inventory" },
    { label: "Trades", href: "/trades" },
    { label: "Community", href: "/community" },
  ]

  return (
    <motion.nav
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      className={cn(
        "fixed top-0 left-0 right-0 z-50 transition-all duration-300",
        scrolled ? "glass-strong" : "bg-background/80 backdrop-blur-md",
        className,
      )}
    >
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex h-16 items-center justify-between gap-4">
          {/* Left Section - Menu & Logo */}
          <div className="flex items-center gap-4">
            {/* Mobile Menu Button */}
            <Button
              variant="ghost"
              size="icon-lg"
              className="lg:hidden text-muted-foreground hover:text-foreground"
              onClick={() => setIsMenuOpen(!isMenuOpen)}
            >
              <AnimatePresence mode="wait">
                {isMenuOpen ? (
                  <motion.div
                    key="close"
                    initial={{ rotate: -90, opacity: 0 }}
                    animate={{ rotate: 0, opacity: 1 }}
                    exit={{ rotate: 90, opacity: 0 }}
                    transition={{ duration: 0.15 }}
                  >
                    <X className="h-[21px] w-[21px]" />
                  </motion.div>
                ) : (
                  <motion.div
                    key="menu"
                    initial={{ rotate: 90, opacity: 0 }}
                    animate={{ rotate: 0, opacity: 1 }}
                    exit={{ rotate: -90, opacity: 0 }}
                    transition={{ duration: 0.15 }}
                  >
                    <Menu className="h-[21px] w-[21px]" />
                  </motion.div>
                )}
              </AnimatePresence>
              <span className="sr-only">Toggle menu</span>
            </Button>

            {/* Logo */}
            <Link href="/" className="flex items-center gap-2 shrink-0">
              <div className="relative">
                <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center">
                  <ArrowLeftRight className="h-4 w-4 text-background" />
                </div>
                <div className="absolute inset-0 rounded-lg bg-gradient-to-br from-emerald-400 to-teal-500 blur-lg opacity-40" />
              </div>
              <span className="text-lg font-bold hidden sm:block">
                <span className="text-gradient">Smart</span>
                <span className="text-foreground">Trade</span>
              </span>
            </Link>

            {/* Desktop Nav Items */}
            <div className="hidden lg:flex items-center gap-1 ml-6">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground transition-colors rounded-md hover:bg-muted/50"
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </div>

          {/* Center Section - Search */}
          <div className="flex-1 max-w-md hidden md:block">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                type="search"
                placeholder="Search items, traders, collections..."
                className="w-full pl-10 bg-muted/50 border-border/50 focus:border-primary/50 focus:bg-muted transition-all"
              />
            </div>
          </div>

          {/* Right Section - Actions */}
          <div className="flex items-center gap-2">
            {/* Mobile Search Toggle */}
            <Button
              variant="ghost"
              size="icon-lg"
              className="md:hidden text-muted-foreground hover:text-foreground"
              onClick={() => setIsSearchOpen(!isSearchOpen)}
            >
              <Search className="h-[21px] w-[21px]" />
              <span className="sr-only">Search</span>
            </Button>

            {/* Wishlist */}
            <Button
              variant="ghost"
              size="icon-lg"
              className="icon-glow icon-glow-violet hidden sm:flex relative
             hover:bg-transparent dark:hover:bg-transparent"
            >
              <Heart className="h-[21px] w-[21px]" />
              <span className="sr-only">Wishlist</span>
            </Button>

            {/* Notifications */}
            <Notification />

            {/* Trades */}
            <Link
              href="/trades"
              onClick={() => {
                void markModuleNotificationsRead("TRADE")
              }}
            >
              <Button
                variant="ghost"
                size="icon-lg"
                className="icon-glow icon-glow-gold relative
               hover:bg-transparent dark:hover:bg-transparent"
              >
                <ArrowLeftRight className="h-[21px] w-[21px]" />

                {tradeAlertCount > 0 && (
                  <span className="nav-count nav-count-warning">{tradeAlertCount > 99 ? "99+" : tradeAlertCount}</span>
                )}

                <span className="sr-only">Trade offers</span>
              </Button>
            </Link>

            {/* Messages */}
            <Link
              href="/messages"
              onClick={() => {
                void markModuleNotificationsRead("CHAT_CONVERSATION")
              }}
            >
              <Button
                variant="ghost"
                size="icon-lg"
                className="icon-glow icon-glow-blue relative
               hover:bg-transparent dark:hover:bg-transparent"
              >
                <MessageCircle className="h-[21px] w-[21px]" />

                {chatAlertCount > 0 && <span className="nav-count nav-count-info">{chatAlertCount > 99 ? "99+" : chatAlertCount}</span>}

                <span className="sr-only">Messages</span>
              </Button>
            </Link>
            {/* Profile / Account Menu */}
            {user ? (
              <DropdownMenu modal={false}>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    className="hidden sm:flex items-center gap-2 px-1.5
             hover:bg-transparent
             dark:hover:bg-transparent
             hover:text-inherit
             dark:hover:text-inherit"
                  >
                    <Avatar className="profile-avatar h-8 w-8">
                      <AvatarImage src={user.avatarUrl} />
                      <AvatarFallback className="bg-muted text-muted-foreground">{user.name.slice(0, 2).toUpperCase()}</AvatarFallback>
                    </Avatar>

                    <ChevronDown className="h-4 w-4 text-muted-foreground transition-transform duration-200 [[data-state=open]_&]:rotate-180" />
                  </Button>
                </DropdownMenuTrigger>

                <DropdownMenuContent align="end" sideOffset={10} className="w-64 glass-strong border-border/60 p-2">
                  <DropdownMenuLabel className="px-3 py-2">
                    <div className="flex items-center gap-3">
                      <Avatar className="h-10 w-10">
                        <AvatarImage src={user.avatarUrl} />
                        <AvatarFallback className="bg-muted">{user.name.slice(0, 2).toUpperCase()}</AvatarFallback>
                      </Avatar>

                      <div className="min-w-0">
                        <p className="truncate font-medium">{user.name}</p>

                        <p className="truncate text-xs text-muted-foreground">{user.email}</p>
                      </div>
                    </div>
                  </DropdownMenuLabel>

                  <DropdownMenuSeparator />

                  <DropdownMenuGroup>
                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/profile" className="cursor-pointer">
                        <User className="mr-2 h-4 w-4" />
                        Profile
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/inventory" className="cursor-pointer">
                        <Package className="mr-2 h-4 w-4" />
                        My Inventory
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link
                        href="/trades"
                        className="cursor-pointer"
                        onClick={() => {
                          void markModuleNotificationsRead("TRADE")
                        }}
                      >
                        <ArrowLeftRight className="mr-2 h-4 w-4" />
                        My Trades
                        {tradeAlertCount > 0 && <span className="ml-auto text-xs text-warning">{tradeAlertCount}</span>}
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/wishlist" className="cursor-pointer">
                        <Heart className="mr-2 h-4 w-4" />
                        Wishlist
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/notifications" className="cursor-pointer">
                        <Bell className="mr-2 h-4 w-4" />
                        Notifications
                        {notificationCount > 0 && <span className="ml-auto text-xs text-primary">{notificationCount}</span>}
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link
                        href="/messages"
                        className="cursor-pointer"
                        onClick={() => {
                          void markModuleNotificationsRead("CHAT_CONVERSATION")
                        }}
                      >
                        <MessageCircle className="mr-2 h-4 w-4" />
                        Messages
                        {chatAlertCount > 0 && <span className="ml-auto text-xs text-info">{chatAlertCount}</span>}
                      </Link>
                    </DropdownMenuItem>
                  </DropdownMenuGroup>

                  <DropdownMenuSeparator />

                  <DropdownMenuSeparator />

                  <DropdownMenuItem asChild className="premium-menu-item">
                    <Link href="/settings">
                      <Settings className="mr-2.5 h-4 w-4" />
                      Settings
                    </Link>
                  </DropdownMenuItem>

                  <DropdownMenuSeparator />

                  <DropdownMenuItem
                    onClick={() => void handleLogout()}
                    className="premium-menu-item cursor-pointer text-destructive focus:text-destructive"
                  >
                    <LogOut className="mr-2.5 h-4 w-4" />
                    Log out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <Link href="/login">
                <Button variant="trade">Login</Button>
              </Link>
            )}

            {/* Mobile Profile */}
            <Avatar className="h-8 w-8 sm:hidden ring-2 ring-primary/30">
              <AvatarImage src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=80&h=80&fit=crop&crop=face" />
              <AvatarFallback className="bg-muted text-muted-foreground">JD</AvatarFallback>
            </Avatar>
          </div>
        </div>
      </div>

      {/* Mobile Search Overlay */}
      <AnimatePresence>
        {isSearchOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="md:hidden border-t border-border/50"
          >
            <div className="p-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search items, traders, collections..."
                  className="w-full pl-10 bg-muted/50 border-border/50"
                  autoFocus
                />
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Mobile Menu Overlay */}
      <AnimatePresence>
        {isMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="lg:hidden border-t border-border/50 glass-strong"
          >
            <div className="p-4 space-y-1">
              {navItems.map((item, index) => (
                <motion.div
                  key={item.href}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: index * 0.05 }}
                >
                  <Link
                    href={item.href}
                    className="block px-4 py-3 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-lg transition-colors"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    {item.label}
                  </Link>
                </motion.div>
              ))}
              <div className="pt-4 border-t border-border/50">
                <Link
                  href="/wishlist"
                  className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-lg transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  <Heart className="h-4 w-4" />
                  Wishlist
                </Link>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.nav>
  )
}
