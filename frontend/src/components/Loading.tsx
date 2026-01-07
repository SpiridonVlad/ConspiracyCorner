export default function Loading() {
  return (
    <div className="flex flex-col items-center justify-center py-12">
      <div className="relative">
        <div className="w-16 h-16 border-4 border-green-900 rounded-full animate-spin border-t-green-400"></div>
        <span className="absolute inset-0 flex items-center justify-center text-2xl">
          👁️
        </span>
      </div>
      <p className="mt-4 text-gray-400 animate-pulse">
        Decrypting the truth...
      </p>
    </div>
  );
}
