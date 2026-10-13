/** @type {import('next').NextConfig} */

const backendOrigin = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/+$/, "");

const nextConfig = {
  typescript: {
    ignoreBuildErrors: false,
  },

  images: {
    unoptimized: true,
    remotePatterns: [
      {
        protocol: "https",
        hostname: "res.cloudinary.com",
        pathname: "/**",
      },
    ],
  },

  async rewrites() {
    if (!backendOrigin) {
      throw new Error(
        "NEXT_PUBLIC_API_BASE_URL must contain the absolute Spring Boot backend origin."
      );
    }

    return [
      {
        source: "/api/backend/:path*",
        destination: `${backendOrigin}/:path*`,
      },
    ];
  },
};

export default nextConfig;