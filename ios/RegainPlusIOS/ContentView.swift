import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("Home", systemImage: "house.fill") }
            SectionListView(title: "Focus", subtitle: "Schedules, focus timer, and blocking tools (iOS implementation in progress).", icon: "timer")
                .tabItem { Label("Focus", systemImage: "timer") }
            SectionListView(title: "Study", subtitle: "Subjects, sessions, tasks, flashcards, and resources.", icon: "book.fill")
                .tabItem { Label("Study", systemImage: "book.fill") }
            SectionListView(title: "Body", subtitle: "Gym, calories, water, weight, and progress tracking.", icon: "figure.strengthtraining.traditional")
                .tabItem { Label("Body", systemImage: "figure.strengthtraining.traditional") }
            MoreView()
                .tabItem { Label("More", systemImage: "square.grid.2x2") }
        }
        .tint(RPTheme.accent)
        .background(RPTheme.bg)
    }
}

struct HomeView: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    headerCard
                    statGrid
                    infoCard
                }
                .padding(16)
            }
            .background(RPTheme.bg.ignoresSafeArea())
            .navigationTitle("RegainPlus")
        }
    }

    private var headerCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Personal, offline-first")
                .font(.caption)
                .foregroundStyle(RPTheme.accent)
            Text("Focus, study, body, wardrobe, skills, and mind — in one place.")
                .font(.headline)
                .foregroundStyle(RPTheme.text)
            Text("iOS initial version (Android app also included in this repo).")
                .font(.subheadline)
                .foregroundStyle(RPTheme.muted)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RPTheme.card)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private var statGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            StatCard(value: "—", label: "Streak", accent: RPTheme.accent)
            StatCard(value: "—", label: "Focus", accent: RPTheme.accent2)
            StatCard(value: "—", label: "Water", accent: RPTheme.teal)
            StatCard(value: "—", label: "Mood", accent: RPTheme.accent)
        }
    }

    private var infoCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("What’s next on iOS")
                .font(.headline)
                .foregroundStyle(RPTheme.text)
            Text("Port schedules/alarms, study and body tracking, wardrobe and skills, and mind tools with iOS-native behavior. System-wide blocking features from Android will require different iOS approaches.")
                .font(.subheadline)
                .foregroundStyle(RPTheme.muted)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RPTheme.card)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

struct StatCard: View {
    let value: String
    let label: String
    let accent: Color

    var body: some View {
        VStack(spacing: 6) {
            Text(value)
                .font(.title2.bold())
                .foregroundStyle(accent)
            Text(label)
                .font(.caption)
                .foregroundStyle(RPTheme.muted)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(RPTheme.cardHi)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

struct SectionListView: View {
    let title: String
    let subtitle: String
    let icon: String

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.system(size: 40))
                    .foregroundStyle(RPTheme.accent)
                Text(title)
                    .font(.title2.bold())
                    .foregroundStyle(RPTheme.text)
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundStyle(RPTheme.muted)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                Text("Initial iOS version — feature implementation in progress.")
                    .font(.caption)
                    .foregroundStyle(RPTheme.muted)
                    .padding(.top, 8)
                Spacer()
            }
            .padding(24)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(RPTheme.bg.ignoresSafeArea())
            .navigationTitle(title)
        }
    }
}

struct MoreView: View {
    var body: some View {
        NavigationStack {
            List {
                NavigationLink("Wardrobe") { SectionListView(title: "Wardrobe", subtitle: "Closet, outfits, capsules, and style planning.", icon: "tshirt.fill") }
                NavigationLink("Skills") { SectionListView(title: "Skills", subtitle: "Track skills, practice time, and progress.", icon: "star.fill") }
                NavigationLink("Mind") { SectionListView(title: "Mind", subtitle: "Mood check-ins, breathing, grounding, and notes.", icon: "brain.head.profile") }
                NavigationLink("Settings") { SectionListView(title: "Settings", subtitle: "Themes, preferences, backup/restore, and permissions (iOS-specific).", icon: "gearshape.fill") }
            }
            .scrollContentBackground(.hidden)
            .background(RPTheme.bg)
            .navigationTitle("More")
        }
    }
}