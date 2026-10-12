"use client"

import { addWishlistItem, getWishlist, removeWishlistItem } from "@/lib/api/wishlist/client"

let cachedIds: Set<number> | null = null
let pendingLoad: Promise<Set<number>> | null = null
let authGeneration = 0
const listeners = new Set<(ids: ReadonlySet<number>, reset?: boolean) => void>()

function publish(reset = false) {
  if (!cachedIds) {
    if (reset) listeners.forEach((listener) => listener(new Set<number>(), true))
    return
  }
  const snapshot = new Set(cachedIds)
  listeners.forEach((listener) => listener(snapshot, reset))
}

/**
 * Clears the in-memory snapshot on sign-out or when the authenticated account changes.
 * The generation guard prevents an old account's in-flight GET from repopulating the cache.
 */
export function resetWishlistCache() {
  authGeneration += 1
  cachedIds = null
  pendingLoad = null
  publish(true)
}

/** A shared in-memory snapshot prevents every marketplace card from making its own GET request. */
export async function loadWishlistIds(force = false): Promise<Set<number>> {
  if (force) resetWishlistCache()
  if (cachedIds) return new Set(cachedIds)

  if (!pendingLoad) {
    const generation = authGeneration
    let request: Promise<Set<number>>
    request = getWishlist()
      .then((items) => {
        const loaded = new Set(items.map((entry) => entry.itemId))
        if (generation !== authGeneration) return new Set(cachedIds ?? [])
        cachedIds = loaded
        publish()
        return new Set(loaded)
      })
      .finally(() => {
        if (pendingLoad === request) pendingLoad = null
      })
    pendingLoad = request
  }

  return new Set(await pendingLoad)
}

export function primeWishlistIds(itemIds: number[]) {
  cachedIds = new Set(itemIds)
  publish()
}

export function subscribeWishlist(listener: (ids: ReadonlySet<number>, reset?: boolean) => void) {
  listeners.add(listener)
  if (cachedIds) listener(new Set(cachedIds))
  return () => listeners.delete(listener)
}

/** Mutates the backend first and updates the shared snapshot only after success. */
export async function setWishlistSaved(itemId: number, saved: boolean): Promise<void> {
  const current = await loadWishlistIds()
  if (current.has(itemId) === saved) return

  if (saved) {
    await addWishlistItem(itemId)
    cachedIds?.add(itemId)
  } else {
    await removeWishlistItem(itemId)
    cachedIds?.delete(itemId)
  }
  publish()
}
