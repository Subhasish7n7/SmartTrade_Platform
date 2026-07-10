import type { Metadata, Viewport } from 'next'
import { Geist, Geist_Mono } from 'next/font/google'
import { Analytics } from '@vercel/analytics/next'
import './globals.css'
import { Providers } from "./providers"

const geist = Geist({ 
  subsets: ["latin"],
  variable: "--font-geist-sans",
});

const geistMono = Geist_Mono({ 
  subsets: ["latin"],
  variable: "--font-geist-mono",
});

export const metadata: Metadata = {
  title: 'TradeVault | Premium Item Trading Marketplace',
  description: 'The premier negotiation-focused trading platform for collectors and enthusiasts. Trade items, negotiate offers, and connect with trusted traders.',
  generator: 'v0.app',
  keywords: ['trading', 'marketplace', 'gaming', 'collectibles', 'items', 'negotiation'],
  icons: {
    icon: [
      {
        url: '/icon-light-32x32.png',
        media: '(prefers-color-scheme: light)',
      },
      {
        url: '/icon-dark-32x32.png',
        media: '(prefers-color-scheme: dark)',
      },
      {
        url: '/icon.svg',
        type: 'image/svg+xml',
      },
    ],
    apple: '/apple-icon.png',
  },
}

export const viewport: Viewport = {
  themeColor: '#0d0f14',
  colorScheme: 'dark',
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en" className="dark bg-background">
      <body className={`${geist.variable} ${geistMono.variable} font-sans antialiased`}>
       <Providers>
        {children}
        {process.env.NODE_ENV === 'production' && <Analytics />}
       </Providers>
      </body>
    </html>
  )
}
