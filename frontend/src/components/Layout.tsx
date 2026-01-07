import { Outlet } from 'react-router-dom';
import Header from './Header';

export default function Layout() {
  return (
    <div className="min-h-screen bg-gray-950 flex flex-col">
      <Header />
      <main className="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
      <footer className="bg-gray-900 border-t border-green-900/30 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <p className="text-gray-500 text-sm">
            👁️ The Truth Is Out There 👁️
          </p>
          <p className="text-gray-600 text-xs mt-2">
            © 2024 Truth Forum - Where Conspiracy Meets Reality
          </p>
        </div>
      </footer>
    </div>
  );
}
