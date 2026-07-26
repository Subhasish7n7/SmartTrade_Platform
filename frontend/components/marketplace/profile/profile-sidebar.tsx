"use client"

import {
    User,
    Package,
    RefreshCw,
    ArrowLeftRight,
    Star,
    History,
    Shield,
    Settings
} from "lucide-react"

import { ProfileTab } from "./profile-layout"

const items = [

    {
        title: "Overview",
        value: "overview",
        icon: User,
    },

    {
        title: "Inventory",
        value: "inventory",
        icon: Package,
    },

    {
        title: "Trade Inventory",
        value: "trade-inventory",
        icon: RefreshCw,
    },

    {
        title: "Trades",
        value: "trades",
        icon: ArrowLeftRight,
    },

    {
        title: "Reviews",
        value: "reviews",
        icon: Star,
    },

    {
        title: "Activity",
        value: "activity",
        icon: History,
    },

    {
        title: "Security",
        value: "security",
        icon: Shield,
    },

    {
        title: "Settings",
        value: "settings",
        icon: Settings,
    },

]

interface Props {

    tab: ProfileTab

    onChange(tab: ProfileTab): void
}

export function ProfileSidebar({
    tab,
    onChange,
}: Props) {

    return (

        <aside className="content-surface h-fit p-2">

            <div className="px-4 py-3">

                <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">

                    Profile

                </p>

            </div>

            <div className="space-y-1">

                {items.map((item) => {

                    const Icon = item.icon

                    const active =
                        tab === item.value

                    return (

                        <button
                            key={item.value}
                            onClick={() =>
                                onChange(item.value as ProfileTab)
                            }
                            className={`w-full rounded-xl px-4 py-3 transition flex items-center gap-3

                                ${active
                                    ? "bg-primary text-primary-foreground"
                                    : "hover:bg-accent"
                                }
                            `}
                        >

                            <Icon className="h-5 w-5" />

                            <span>

                                {item.title}

                            </span>

                        </button>

                    )

                })}

            </div>

        </aside>

    )

}