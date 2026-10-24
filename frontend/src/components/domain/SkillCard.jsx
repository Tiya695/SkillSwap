import { BookOpen, GraduationCap } from 'lucide-react'
import Badge from '../ui/Badge'
import Button from '../ui/Button'
import Card from '../ui/Card'

export default function SkillCard({ skill, onAdd }) {
  return (
    <Card tilt gradient className="flex flex-col p-5">
      <Badge className="self-start">{skill.category}</Badge>
      <h3 className="mt-3 text-lg font-bold">{skill.name}</h3>
      <p className="mt-1 flex-1 text-sm text-muted">{skill.description || 'No description yet.'}</p>
      {onAdd && (
        <div className="mt-5 flex flex-wrap gap-2">
          <Button size="sm" onClick={() => onAdd(skill, 'OFFERED')}>
            <GraduationCap className="h-4 w-4" aria-hidden="true" /> I can teach
          </Button>
          <Button size="sm" variant="secondary" onClick={() => onAdd(skill, 'WANTED')}>
            <BookOpen className="h-4 w-4" aria-hidden="true" /> I want to learn
          </Button>
        </div>
      )}
    </Card>
  )
}
