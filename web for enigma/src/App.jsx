import { useState, useRef, useEffect } from 'react'
import axios from 'axios'

function App() {
  const [query, setQuery] = useState('')
  const [conversation, setConversation] = useState([])
  const messagesEndRef = useRef(null)

  // Load conversation from localStorage on mount, but clear if backend is unavailable
  useEffect(() => {
    const checkBackendAndLoadConversation = async () => {
      try {
        // Try to ping the backend to verify it's running
        await axios.post('http://localhost:8080/enigma/ai', {
          query: ''
        }, { timeout: 3000 })
        
        // Backend is running, load conversation from localStorage
        const saved = localStorage.getItem('enigma_conversation')
        if (saved) {
          try {
            setConversation(JSON.parse(saved))
          } catch (e) {
            console.error('Failed to load conversation:', e)
          }
        }
      } catch (err) {
        // Backend is not available, clear the conversation history
        console.log('Backend unavailable on startup, clearing conversation history')
        localStorage.removeItem('enigma_conversation')
        setConversation([])
      }
    }
    
    checkBackendAndLoadConversation()
  }, [])

  // Save conversation to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem('enigma_conversation', JSON.stringify(conversation))
  }, [conversation])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [conversation])

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!query.trim()) {
      return
    }

    const userQuery = query
    setQuery('')
    
    // Immediately add the user message to conversation
    const messageId = Date.now()
    setConversation(prev => [...prev, {
      id: messageId,
      userQuery,
      answer: null,
      sql: null,
      error: null,
      loading: true
    }])

    try {
      const response = await axios.post('http://localhost:8080/enigma/ai', {
        query: userQuery.trim()
      })

      if (response.data) {
        const { answer, sql } = response.data
        
        if (answer) {
          // Update the message with the response
          setConversation(prev => prev.map(msg =>
            msg.id === messageId
              ? {
                  ...msg,
                  answer,
                  sql: sql || 'N/A - Question not related to the Enigma Machine',
                  error: null,
                  loading: false
                }
              : msg
          ))
        } else {
          setConversation(prev => prev.map(msg =>
            msg.id === messageId
              ? {
                  ...msg,
                  answer: null,
                  sql: null,
                  error: 'No response received from the server',
                  loading: false
                }
              : msg
          ))
        }
      } else {
        setConversation(prev => prev.map(msg =>
          msg.id === messageId
            ? {
                ...msg,
                answer: null,
                sql: null,
                error: 'Invalid response format from server',
                loading: false
              }
            : msg
        ))
      }
    } catch (err) {
      let errorMsg = 'Unknown error'
      if (err.response) {
        errorMsg = `Server error: ${err.response.status} - ${err.response.statusText}`
      } else if (err.request) {
        errorMsg = 'Cannot connect to the server. Make sure the Spring Boot backend is running at http://localhost:8080'
      } else {
        errorMsg = `Error: ${err.message}`
      }
      
      setConversation(prev => prev.map(msg =>
        msg.id === messageId
          ? {
              ...msg,
              answer: null,
              sql: null,
              error: errorMsg,
              loading: false
            }
          : msg
      ))
    }
  }

  const deleteMessage = (id) => {
    setConversation(prev => prev.filter(msg => msg.id !== id))
  }

  const clearConversation = () => {
    if (window.confirm('Are you sure you want to clear all messages?')) {
      setConversation([])
    }
  }

  return (
    <div className="h-screen flex flex-col bg-white">
      {/* Header */}
      <div className="border-b border-gray-200 bg-white px-6 py-4 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Enigma Machine Management System
          </h1>
          <p className="text-sm text-gray-500 mt-1">Ask a question to query the Enigma Machine database</p>
        </div>
        {conversation.length > 0 && (
          <button
            onClick={clearConversation}
            className="text-sm text-red-500 hover:text-red-700 font-semibold"
          >
            Clear All
          </button>
        )}
      </div>

      {/* Messages Container */}
      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {/* Empty State */}
        {conversation.length === 0 && (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <p className="text-gray-400 text-lg">
                Ask a question to get started
              </p>
            </div>
          </div>
        )}

        {/* Conversation History */}
        {conversation.map((msg) => (
          <div key={msg.id} className="space-y-2">
            {/* User Query */}
            <div className="flex justify-end">
              <div className="bg-blue-500 text-white rounded-lg px-4 py-2 max-w-2xl group relative">
                <p className="text-sm whitespace-pre-wrap break-words">{msg.userQuery}</p>
                <button
                  onClick={() => deleteMessage(msg.id)}
                  className="absolute -right-8 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition text-xs"
                >
                  ✕
                </button>
              </div>
            </div>

            {/* Loading Indicator */}
            {msg.loading && (
              <div className="flex justify-start">
                <div className="bg-gray-100 rounded-lg px-4 py-2">
                  <div className="flex items-center gap-2">
                    <span className="animate-spin">⟳</span>
                    <p className="text-sm text-gray-700">Processing...</p>
                  </div>
                </div>
              </div>
            )}

            {/* Error Message */}
            {msg.error && (
              <div className="flex justify-start">
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2 max-w-2xl">
                  <p className="font-semibold text-sm">Error</p>
                  <p className="text-sm mt-1">{msg.error}</p>
                </div>
              </div>
            )}

            {/* Response */}
            {msg.answer && !msg.error && (
              <div className="flex justify-start space-y-4">
                <div className="max-w-2xl space-y-3 w-full">
                  {/* Answer Panel */}
                  <div className="bg-gray-100 rounded-lg p-4">
                    <h3 className="text-sm font-semibold text-gray-900 mb-2">Answer</h3>
                    <div className="text-gray-700 text-sm whitespace-pre-wrap leading-relaxed max-h-64 overflow-y-auto">
                      {msg.answer}
                    </div>
                  </div>

                  {/* SQL Panel */}
                  <div className="bg-gray-100 rounded-lg p-4">
                    <h3 className="text-sm font-semibold text-gray-900 mb-2">SQL Query</h3>
                    <div className="bg-gray-900 rounded p-3 font-mono text-xs text-amber-400 whitespace-pre-wrap max-h-64 overflow-y-auto">
                      {msg.sql}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        ))}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Section - Fixed at Bottom */}
      <div className="border-t border-gray-200 bg-white p-6">
        <form onSubmit={handleSubmit} className="flex gap-3">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Ask a question..."
            className="flex-1 px-4 py-3 rounded-lg bg-gray-100 text-gray-900 placeholder-gray-500 border border-gray-200 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition"
          />
          <button
            type="submit"
            disabled={!query.trim()}
            className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition duration-200 flex items-center justify-center gap-2"
          >
            Send
          </button>
        </form>
      </div>
    </div>
  )
}

export default App
