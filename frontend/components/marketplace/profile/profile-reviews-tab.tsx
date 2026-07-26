// profile-reviews-tab.tsx
"use client"

import { Star } from "lucide-react"

import { ProfileReview } from "@/app/profile/page"

interface Props {
  reviews: ProfileReview[]
}

export function ProfileReviewsTab({
  reviews,
}: Props) {

  if (!reviews.length) {
    return (
      <div className="content-surface py-20 text-center">

        <Star className="mx-auto h-12 w-12 text-muted-foreground" />

        <h2 className="mt-5 text-2xl font-semibold">
          No Reviews Yet
        </h2>

      </div>
    )
  }

  return (
    <div className="space-y-5">

      {reviews.map((review) => (

        <div
          key={review.id}
          className="content-surface"
        >

          <div className="flex items-center justify-between">

            <div>

              <h3 className="font-semibold">
                {review.user}
              </h3>

              <p className="text-sm text-muted-foreground">
                {review.createdAt}
              </p>

            </div>

            <div className="flex items-center gap-1">

              <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />

              {review.rating}

            </div>

          </div>

          <p className="mt-5 leading-relaxed text-muted-foreground">
            {review.comment}
          </p>

        </div>

      ))}

    </div>
  )
}