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
import {
  ArrowLeftRight,
  Bell,
  ChevronDown,
  Heart,
  LayoutDashboard,
  LogOut,
  Menu,
  MessageCircle,
  Package,
  Search,
  Settings,
  User,
  X,
} from "lucide-react"
import Link from "next/link"
import { usePathname, useRouter, useSearchParams } from "next/navigation"
import * as React from "react"
import { toast } from "sonner"

import { Notification } from "./notification"
import { resetWishlistCache } from "@/lib/api/wishlist/store"

interface NavbarProps {
  className?: string
}

const navItems = [
  { label: "Marketplace", href: "/marketplace" },
  { label: "My Inventory", href: "/inventory" },
  { label: "Trades", href: "/trades" },
  { label: "Community", href: "/community" },
]

export function Navbar({ className }: NavbarProps) {
  const [isMenuOpen, setIsMenuOpen] = React.useState(false)
  const [isSearchOpen, setIsSearchOpen] = React.useState(false)
  const [scrolled, setScrolled] = React.useState(false)
  const [searchValue, setSearchValue] = React.useState("")

  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const { user, setUser } = useAuth()

  const { unreadCount: notificationCount, tradeAlertCount, chatAlertCount, markModuleNotificationsRead } = useNotifications()

  React.useEffect(() => {
    const queryName = searchParams.get("name") ?? ""
    setSearchValue(queryName)
  }, [searchParams])

  React.useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 16)
    }

    window.addEventListener("scroll", handleScroll, {
      passive: true,
    })

    return () => window.removeEventListener("scroll", handleScroll)
  }, [])

  const submitSearch = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const trimmed = searchValue.trim()
    const nextUrl = trimmed ? `/marketplace?name=${encodeURIComponent(trimmed)}` : "/marketplace"

    setIsSearchOpen(false)
    setIsMenuOpen(false)

    router.push(nextUrl)
  }

  const clearSearch = () => {
    setSearchValue("")
    setIsSearchOpen(false)
    setIsMenuOpen(false)
    router.push("/marketplace")
  }

  const handleLogout = async () => {
    try {
      await stompClient.deactivate()
      await logout()
      setUser(null)
      resetWishlistCache()
      router.refresh()
    } catch (error) {
      console.error("Logout failed", error)
      toast.error("Failed to log out")
    }
  }

  const isActive = (href: string) => {
    if (href === "/marketplace") {
      return pathname === "/marketplace"
    }

    return pathname.startsWith(href)
  }

  return (
    <motion.nav
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      className={cn("marketplace-navbar", scrolled && "marketplace-navbar-scrolled", className)}
    >
      <div className="mx-auto max-w-[1600px] px-3 sm:px-5 lg:px-7">
        <div className="flex min-h-16 items-center gap-3 py-2 lg:gap-4">
          <Button
            variant="ghost"
            size="icon-lg"
            className="lg:hidden text-muted-foreground hover:bg-white/[0.04] hover:text-foreground"
            onClick={() => setIsMenuOpen((open) => !open)}
          >
            <AnimatePresence mode="wait">
              {isMenuOpen ? (
                <motion.div
                  key="close"
                  initial={{ rotate: -90, opacity: 0 }}
                  animate={{ rotate: 0, opacity: 1 }}
                  exit={{ rotate: 90, opacity: 0 }}
                >
                  <X className="h-[21px] w-[21px]" />
                </motion.div>
              ) : (
                <motion.div
                  key="menu"
                  initial={{ rotate: 90, opacity: 0 }}
                  animate={{ rotate: 0, opacity: 1 }}
                  exit={{ rotate: -90, opacity: 0 }}
                >
                  <Menu className="h-[21px] w-[21px]" />
                </motion.div>
              )}
            </AnimatePresence>

            <span className="sr-only">Toggle menu</span>
          </Button>

          <Link href="/" className="marketplace-navbar-brand shrink-0">
            <div className="marketplace-navbar-logo">
              <ArrowLeftRight className="h-4 w-4 text-background" />
            </div>

            <span className="hidden text-lg font-bold sm:block">
              <span className="text-gradient">Smart</span>
              <span className="text-foreground">Trade</span>
            </span>
          </Link>

          <div className="hidden items-center gap-1 lg:flex">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={cn("marketplace-navbar-link", isActive(item.href) && "marketplace-navbar-link-active")}
              >
                {item.label}
              </Link>
            ))}
          </div>

          <div className="hidden min-w-0 flex-1 justify-center md:flex">
            <form onSubmit={submitSearch} className="w-full max-w-2xl">
              <div className="marketplace-navbar-search">
                <Search className="h-[18px] w-[18px] shrink-0 text-muted-foreground" />

                <Input
                  type="search"
                  value={searchValue}
                  onChange={(event) => setSearchValue(event.target.value)}
                  placeholder="Search the marketplace..."
                  aria-label="Search the marketplace"
                  className="h-11 border-0 bg-transparent px-0 text-sm font-medium shadow-none focus-visible:ring-0 placeholder:text-muted-foreground/90"
                />

                {searchValue && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-sm"
                    aria-label="Clear search"
                    onClick={clearSearch}
                    className="marketplace-navbar-search-clear"
                  >
                    <X className="h-3.5 w-3.5" />
                  </Button>
                )}

                <kbd className="hidden rounded-lg border border-white/[0.07] bg-white/[0.035] px-2 py-1 text-[10px] font-medium text-muted-foreground xl:inline-block">
                  Enter
                </kbd>
              </div>
            </form>
          </div>

          <div className="ml-auto flex items-center gap-1.5 sm:gap-2">
            <Button
              variant="ghost"
              size="icon-lg"
              className="md:hidden text-muted-foreground hover:bg-white/[0.04] hover:text-foreground"
              onClick={() => setIsSearchOpen((open) => !open)}
            >
              <Search className="h-[21px] w-[21px]" />
              <span className="sr-only">Search</span>
            </Button>
            <Link href={"/wishlist"}>
            <Button variant="ghost" size="icon-lg" className="icon-glow icon-glow-violet hidden sm:flex relative hover:bg-transparent">
              <Heart className="h-[21px] w-[21px]" />
              <span className="sr-only">Wishlist</span>
            </Button>
            </Link>

            <Notification />

            <Link href="/trades" onClick={() => void markModuleNotificationsRead("TRADE")}>
              <Button variant="ghost" size="icon-lg" className="icon-glow icon-glow-gold relative hover:bg-transparent">
                <ArrowLeftRight className="h-[21px] w-[21px]" />

                {tradeAlertCount > 0 && (
                  <span className="nav-count nav-count-warning">{tradeAlertCount > 99 ? "99+" : tradeAlertCount}</span>
                )}

                <span className="sr-only">Trade offers</span>
              </Button>
            </Link>

            <Link href="/messages" onClick={() => void markModuleNotificationsRead("CHAT_CONVERSATION")}>
              <Button variant="ghost" size="icon-lg" className="icon-glow icon-glow-blue relative hover:bg-transparent">
                <MessageCircle className="h-[21px] w-[21px]" />

                {chatAlertCount > 0 && <span className="nav-count nav-count-info">{chatAlertCount > 99 ? "99+" : chatAlertCount}</span>}

                <span className="sr-only">Messages</span>
              </Button>
            </Link>

            {user ? (
              <DropdownMenu modal={false}>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    className="marketplace-profile-trigger hidden items-center gap-2 px-1.5 hover:bg-transparent sm:flex"
                  >
                    <Avatar className="profile-avatar h-8 w-8">
                      <AvatarImage src={user.avatarUrl} />
                      <AvatarFallback className="bg-gradient-to-br from-[var(--trust-blue)]/20 via-[var(--negotiation-violet)]/15 to-[var(--premium-gold)]/10 text-xs font-bold text-foreground">
                        {user.name.slice(0, 2).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>

                    <ChevronDown className="h-4 w-4 text-muted-foreground" />
                  </Button>
                </DropdownMenuTrigger>

                <DropdownMenuContent align="end" sideOffset={10} className="w-64 glass-strong border-border/60 p-2">
                  <DropdownMenuLabel className="px-3 py-2">
                    <div className="flex items-center gap-3">
                      <Avatar className="h-10 w-10">
                        <AvatarImage src={user.avatarUrl} />
                        <AvatarFallback className="bg-gradient-to-br from-[var(--trust-blue)]/20 via-[var(--negotiation-violet)]/15 to-[var(--premium-gold)]/10">
                          {user.name.slice(0, 2).toUpperCase()}
                        </AvatarFallback>
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
                      <Link href="/profile">
                        <User className="mr-2 h-4 w-4" />
                        Profile
                      </Link>
                    </DropdownMenuItem>
                    {user.role === "ADMIN" && (
                      <DropdownMenuItem asChild className="premium-menu-item">
                        <Link href="/admin">
                          <LayoutDashboard className="mr-2 h-4 w-4 text-[var(--trust-blue)]" />
                          Admin Console
                        </Link>
                      </DropdownMenuItem>
                    )}

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/inventory">
                        <Package className="mr-2 h-4 w-4" />
                        My Inventory
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/trades" onClick={() => void markModuleNotificationsRead("TRADE")}>
                        <ArrowLeftRight className="mr-2 h-4 w-4" />
                        My Trades
                        {tradeAlertCount > 0 && <span className="ml-auto text-xs text-warning">{tradeAlertCount}</span>}
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/wishlist">
                        <Heart className="mr-2 h-4 w-4" />
                        Wishlist
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/notifications">
                        <Bell className="mr-2 h-4 w-4" />
                        Notifications
                        {notificationCount > 0 && <span className="ml-auto text-xs text-primary">{notificationCount}</span>}
                      </Link>
                    </DropdownMenuItem>

                    <DropdownMenuItem asChild className="premium-menu-item">
                      <Link href="/messages" onClick={() => void markModuleNotificationsRead("CHAT_CONVERSATION")}>
                        <MessageCircle className="mr-2 h-4 w-4" />
                        Messages
                        {chatAlertCount > 0 && <span className="ml-auto text-xs text-info">{chatAlertCount}</span>}
                      </Link>
                    </DropdownMenuItem>
                  </DropdownMenuGroup>

                  <DropdownMenuSeparator />

                  <DropdownMenuItem asChild className="premium-menu-item">
                    <Link href="/profile?isSettings=true">
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
                <Button variant="trade" className="rounded-xl">
                  Login
                </Button>
              </Link>
            )}

            {user && (
              <Avatar className="h-8 w-8 sm:hidden ring-2 ring-[var(--trust-blue)]/25">
                <AvatarImage src={user.avatarUrl} />
                <AvatarFallback className="bg-gradient-to-br from-[var(--trust-blue)]/20 via-[var(--negotiation-violet)]/15 to-[var(--premium-gold)]/10 text-xs font-bold">
                  {user.name.slice(0, 2).toUpperCase()}
                </AvatarFallback>
              </Avatar>
            )}
          </div>
        </div>
      </div>

      <AnimatePresence>
        {isSearchOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="border-t border-white/[0.06] md:hidden"
          >
            <form onSubmit={submitSearch} className="p-3 sm:p-4">
              <div className="marketplace-navbar-search">
                <Search className="h-[18px] w-[18px] shrink-0 text-muted-foreground" />

                <Input
                  type="search"
                  value={searchValue}
                  onChange={(event) => setSearchValue(event.target.value)}
                  placeholder="Search the marketplace..."
                  aria-label="Search the marketplace"
                  autoFocus
                  className="h-11 border-0 bg-transparent px-0 text-sm font-medium shadow-none focus-visible:ring-0"
                />

                {searchValue && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-sm"
                    aria-label="Clear search"
                    onClick={clearSearch}
                    className="marketplace-navbar-search-clear"
                  >
                    <X className="h-3.5 w-3.5" />
                  </Button>
                )}
              </div>
            </form>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {isMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="border-t border-white/[0.06] glass-strong lg:hidden"
          >
            <div className="space-y-1 p-4">
              {navItems.map((item, index) => (
                <motion.div
                  key={item.href}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: index * 0.04 }}
                >
                  <Link
                    href={item.href}
                    className={cn("marketplace-navbar-mobile-link", isActive(item.href) && "marketplace-navbar-mobile-link-active")}
                    onClick={() => setIsMenuOpen(false)}
                  >
                    {item.label}
                  </Link>
                </motion.div>
              ))}

              <div className="my-3 h-px bg-gradient-to-r from-transparent via-border/60 to-transparent" />

              <Link href="/wishlist" className="marketplace-navbar-mobile-link" onClick={() => setIsMenuOpen(false)}>
                <Heart className="h-4 w-4" />
                Wishlist
              </Link>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.nav>
  )
}
