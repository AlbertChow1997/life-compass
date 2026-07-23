import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import RequireRole from './components/RequireRole'
import RequireAuth from './components/RequireAuth'
import SupportWidget from './components/SupportWidget'
import { AuthProvider } from './context/AuthContext'
import ShopListPage from './pages/ShopListPage'
import ShopDetailPage from './pages/ShopDetailPage'
import PostsPage from './pages/PostsPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import MerchantVouchersPage from './pages/MerchantVouchersPage'
import AdminPostsPage from './pages/AdminPostsPage'
import AdminSupportPage from './pages/AdminSupportPage'
import ProfilePage from './pages/ProfilePage'
import FollowedShopsPage from './pages/FollowedShopsPage'
import MyCommentsPage from './pages/MyCommentsPage'
import MyPostsPage from './pages/MyPostsPage'
import MyLikesPage from './pages/MyLikesPage'
import MyOrdersPage from './pages/MyOrdersPage'
import './App.css'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="container">
          <Routes>
            <Route path="/" element={<ShopListPage />} />
            <Route path="/shop/:id" element={<ShopDetailPage />} />
            <Route path="/posts" element={<PostsPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/merchant/vouchers"
              element={
                <RequireRole role="MERCHANT">
                  <MerchantVouchersPage />
                </RequireRole>
              }
            />
            <Route
              path="/admin/posts"
              element={
                <RequireRole role="ADMIN">
                  <AdminPostsPage />
                </RequireRole>
              }
            />
            <Route
              path="/admin/support"
              element={
                <RequireRole role="ADMIN">
                  <AdminSupportPage />
                </RequireRole>
              }
            />
            <Route
              path="/profile"
              element={
                <RequireAuth>
                  <ProfilePage />
                </RequireAuth>
              }
            />
            <Route
              path="/profile/shops"
              element={
                <RequireAuth>
                  <FollowedShopsPage />
                </RequireAuth>
              }
            />
            <Route
              path="/profile/comments"
              element={
                <RequireAuth>
                  <MyCommentsPage />
                </RequireAuth>
              }
            />
            <Route
              path="/profile/posts"
              element={
                <RequireAuth>
                  <MyPostsPage />
                </RequireAuth>
              }
            />
            <Route
              path="/profile/likes"
              element={
                <RequireAuth>
                  <MyLikesPage />
                </RequireAuth>
              }
            />
            <Route
              path="/profile/orders"
              element={
                <RequireAuth>
                  <MyOrdersPage />
                </RequireAuth>
              }
            />
          </Routes>
        </main>
        <SupportWidget />
      </BrowserRouter>
    </AuthProvider>
  )
}
