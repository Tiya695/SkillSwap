import Button from './Button'
import Modal from './Modal'

export default function ConfirmModal({ open, title, text, confirmLabel = 'Delete', loading, onConfirm, onClose }) {
  return (
    <Modal
      open={open}
      onClose={onClose}
      title={title}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="primary" loading={loading} onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </>
      }
    >
      <p className="text-sm text-muted">{text}</p>
    </Modal>
  )
}
